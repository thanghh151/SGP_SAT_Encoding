import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class SocialGolfer {

    static int w; // number of weeks
    static int p; // players per group
    static int g; // number of groups
    static int x; // p * g

    static int timeBudget = 10;

    static ISolver satSolver;

    public static void main(String[] args) {
        mainProgram();
    }

    static void genAllClauses() {
        genClause1();
        genClause2();
        genClause3();
        genClause4();
        genClause5();
        genClause6();
        genClause7();
        genSymmetryBreakingClause1();
        genSymmetryBreakingClause2();
        genSymmetryBreakingClause3();
    }

    // Each golfer plays at least once a week
    static void genClause1() {
        for (int i = 1; i <= x; i++) {
            List<Integer> loo = new ArrayList<>();
            
            // Binary encoding for each variable Xi
            List<Integer> binaryRepresentation = convertToBinary(i);

            // Create k new variables Y1, Y2, ..., Yk
            for (int j = 0; j < binaryRepresentation.size(); j++) {
                loo.add(getVariable(i, 1, j + 1, 1));
            }

            for (int j = 0; j < binaryRepresentation.size(); j++) {
                if (binaryRepresentation.get(j) == 1) {
                    loo.add(getVariable(i, 1, j + 1, 1));
                } else {
                    loo.add(-getVariable(i, 1, j + 1, 1));
                }
            }

            addClause(loo);
        }
    }

    // Function to convert an integer to its binary representation
    static List<Integer> convertToBinary(int num) {
        List<Integer> binaryRepresentation = new ArrayList<>();

        while (num > 0) {
            binaryRepresentation.add(num % 2);
            num /= 2;
        }

        // Reverse the list to get the correct binary representation
        Collections.reverse(binaryRepresentation);

        return binaryRepresentation;
    }

    // Each golfer plays at most once in each group in each week
    static void genClause2() {
        for (int i = 1; i <= x; i++) {
            for (int l = 1; l <= w; l++) {
                for (int j = 1; j <= p; j++) {
                    for (int k = 1; k <= g; k++) {
                        for (int m = j + 1; m <= p; m++) {
                            List<Integer> loo = new ArrayList<>();
                            loo.add(-1 * getVariable(i, j, k, l));
                            loo.add(-1 * getVariable(i, m, k, l));
                            addClause(loo);
                        }
                    }
                }
            }
        }
    }

    // No golfer plays in more than one group in each week
    static void genClause3() {
        for (int i = 1; i <= x; i++) {
            for (int l = 1; l <= w; l++) {
                for (int j = 1; j <= p; j++) {
                    for (int k = 1; k <= g; k++) {
                        for (int m = k + 1; m <= g; m++) {
                            for (int n = 1; n <= p; n++) {
                                List<Integer> loo = new ArrayList<>();
                                loo.add(-1 * getVariable(i, j, k, l));
                                loo.add(-1 * getVariable(i, n, m, l));
                                addClause(loo);
                            }
                        }
                    }
                }
            }
        }
    }

    // Each group has at least p players
    static void genClause4() {
        for (int l = 1; l <= w; l++) {
            for (int k = 1; k <= g; k++) {
                for (int j = 1; j <= p; j++) {
                    List<Integer> loo = new ArrayList<>();
                    for (int i = 1; i <= x; i++) {
                        loo.add(getVariable(i, j, k, l));
                    }
                    addClause(loo);
                }
            }
        }
    }

    // Each group has at most p players
    static void genClause5() {
        for (int l = 1; l <= w; l++) {
            for (int k = 1; k <= g; k++) {
                for (int j = 1; j <= p; j++) {
                    for (int i = 1; i <= x; i++) {
                        for (int m = i + 1; m <= p; m++) {
                            List<Integer> loo = new ArrayList<>();
                            loo.add(-1 * getVariable(i, j, k, l));
                            loo.add(-1 * getVariable(m, j, k, l));
                            addClause(loo);
                        }
                    }
                }
            }
        }
    }

    // This is a clause combining two sets of variables, ijkl and ikl
    static void genClause6() {
        for (int i = 1; i <= x; i++) {
            for (int k = 1; k <= g; k++) {
                for (int l = 1; l <= w; l++) {
                    List<Integer> tab = new ArrayList<>();
                    tab.add(-1 * getVariable2(i, k, l));
                    for (int j = 1; j <= p; j++) {
                        tab.add(getVariable(i, j, k, l));
                        List<Integer> tab2 = new ArrayList<>();
                        tab2.add(getVariable2(i, k, l));
                        tab2.add(-1 * getVariable(i, j, k, l));
                        addClause(tab2);
                    }
                    addClause(tab);
                }
            }
        }
    }

    // If two players m and n play in the same group k in week l, they cannot play
    // together in any group together in future weeks
    static void genClause7() {
        for (int l = 1; l <= w; l++) {
            for (int k = 1; k <= g; k++) {
                for (int m = 1; m <= x; m++) {
                    for (int n = m + 1; n <= x; n++) {
                        for (int k2 = 1; k2 <= g; k2++) {
                            for (int l2 = l + 1; l2 <= w; l2++) {
                                List<Integer> loo = new ArrayList<>();
                                loo.add(-1 * getVariable2(m, k, l));
                                loo.add(-1 * getVariable2(n, k, l));
                                loo.add(-1 * getVariable2(m, k2, l2));
                                loo.add(-1 * getVariable2(n, k2, l2));
                                addClause(loo);
                            }
                        }
                    }
                }
            }
        }
    }

    // Symmetry breaking clause 1
    static void genSymmetryBreakingClause1() {
        for (int i = 1; i <= x; i++) {
            for (int j = 1; j < p; j++) {
                for (int k = 1; k <= g; k++) {
                    for (int l = 1; l <= w; l++) {
                        for (int m = 1; m <= i; m++) {
                            List<Integer> loo = new ArrayList<>();
                            loo.add(-1 * getVariable(i, j, k, l));
                            loo.add(-1 * getVariable(m, j + 1, k, l));
                            addClause(loo);
                        }
                    }
                }
            }
        }
    }

    // Symmetry breaking clause 2
    static void genSymmetryBreakingClause2() {
        for (int i = 1; i <= x; i++) {
            for (int k = 1; k < g; k++) {
                for (int l = 1; l <= w; l++) {
                    for (int m = 1; m < i; m++) {
                        List<Integer> loo = new ArrayList<>();
                        loo.add(-1 * getVariable(i, 1, k, l));
                        loo.add(-1 * getVariable(m, 1, k + 1, l));
                        addClause(loo);
                    }
                }
            }
        }
    }

    // Symmetry breaking clause 3
    static void genSymmetryBreakingClause3() {
        for (int i = 1; i <= x; i++) {
            for (int l = 1; l < w; l++) {
                for (int m = 1; m <= i; m++) {
                    List<Integer> loo = new ArrayList<>();
                    loo.add(-1 * getVariable(i, 2, 1, l));
                    loo.add(-1 * getVariable(m, 2, 1, l + 1));
                    addClause(loo);
                }
            }
        }
    }

    static int getVariable(int i, int j, int k, int l) {
        i -= 1;
        j -= 1;
        k -= 1;
        l -= 1;
        return i + (x * j) + (k * x * p) + (l * x * p * g) + 1;
    }

    static int getVariable2(int i, int k, int l) {
        i -= 1;
        k -= 1;
        l -= 1;
        return i + (x * k) + (l * x * g) + 1 + (x * p * g * w);
    }

    static int resolveVariable(int v) {
        for (int l = 1; l <= w; l++) {
            for (int k = 1; k <= g; k++) {
                for (int j = 1; j <= p; j++) {
                    for (int i = 1; i <= x; i++) {
                        int var = getVariable(i, j, k, l);
                        if (Math.abs(v) == var) {
                            return i;
                        }
                    }
                }
            }
        }
        
        for (int l = 1; l <= w; l++) {
            for (int k = 1; k <= g; k++) {
                for (int i = 1; i <= x; i++) {
                    int var = getVariable2(i, k, l);
                    if (Math.abs(v) == var) {
                        return i;
                    }
                }
            }
        }
        
        return 0;
    }
    
    static long startTime;
    static List<List<Integer>> allClauses = new ArrayList<>();

    static void addClause(List<Integer> clause) {
        int[] array = clause.stream().mapToInt(i -> i).toArray();
        try {
            satSolver.addClause(new VecInt(array));
            allClauses.add(new ArrayList<>(clause));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void interrupt(ISolver solver) {
        solver.setTimeout(1);
    }

    static void solveSatProblem() {
        x = p * g;
    
        System.out.println("\nGenerating the problem.");
    
        satSolver = SolverFactory.newDefault();
        satSolver.setTimeout(timeBudget);
        
        // Thêm dòng này để reset solver
        satSolver.reset();
    
        genAllClauses();
    
        startTime = System.currentTimeMillis();
        System.out.println("Clauses: " + satSolver.nConstraints());
        System.out.println("Variables: " + satSolver.nVars());
    
        System.out.println("\nSearching for a solution.");
    
        Thread timer = new Thread(() -> interrupt(satSolver));
        timer.start();
    
        boolean satStatus;
        try {
            satStatus = satSolver.isSatisfiable();
        } catch (TimeoutException e) {
            satStatus = false;
        }

        if (!satStatus) {
            System.out.println("\nNot found. No solution exists.");
        } else {
            int[] solution = satSolver.model();
            if (solution == null) {
                System.out.println("Not found. Exceeded time limit (" + timeBudget + "s).\n");
            } else {
                long endTime = System.currentTimeMillis();
                System.out.println("Solution found in " + (endTime - startTime) + " milliseconds. Generating it.\n");
                List<Integer> result = new ArrayList<>();
                for (int v : solution) {
                    if (v > 0) {
                        int ijkl = resolveVariable(v);
                        if (ijkl != 0) {
                            result.add(ijkl);
                        }
                    }
                }
                List<Integer>[][] finalResult = processResults(result);
                showResults(finalResult);

                // Ghi ra file CNF
                try {
                    String directoryPath = "input_v1";
                    
                    // Tạo thư mục nếu nó không tồn tại
                    File directory = new File(directoryPath);
                    if (!directory.exists()) {
                        directory.mkdir();
                    }

                    // Tạo FileWriter với đường dẫn đầy đủ đến file "result.cnf" trong thư mục "input_v1"
                    FileWriter writer = new FileWriter(directoryPath + "/result.cnf");

                    // Ghi dòng thông tin về số biến và số ràng buộc
                    writer.write("p cnf " + satSolver.nVars() + " " + satSolver.nConstraints() + "\n");

                    // Ghi từng mệnh đề vào file
                    for (List<Integer> clause : allClauses) {
                        for (int literal : clause) {
                            writer.write(literal + " ");
                        }
                        writer.write("0\n");
                    }

                    // Đóng FileWriter
                    writer.close();

                    System.out.println("CNF written to input_v1/result.cnf");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        new Scanner(System.in).nextLine(); // Wait for Enter key
    }

    static List<Integer>[][] processResults(List<Integer> result) {
        List<Integer>[][] ntab = new List[w + 1][g + 1];
    
        for (int week = 1; week <= w; week++) {
            for (int group = 1; group <= g; group++) {
                ntab[week][group] = new ArrayList<>();
            }
        }
    
        int index = 0;
    
        for (int l = 1; l <= w; l++) {
            for (int k = 1; k <= g; k++) {
                for (int i = 1; i <= p; i++) {
                    if (index < result.size()) {
                        int player = result.get(index);
                        ntab[l][k].add(player);
                        index++;
                    }
                }
            }
        }
    
        return ntab;
    }
    
    static void showResults(List<Integer>[][] result) {
        System.out.println("\nResult:");

        // Print header
        System.out.print("+------+");
        for (int group = 1; group <= g; group++) {
            System.out.print("-----------+");
        }
        System.out.println();

        System.out.print("| Week |");
        for (int group = 1; group <= g; group++) {
            System.out.printf(" Group %-2d  |", group);
        }
        System.out.println();

        // Print separator
        System.out.print("+------+");
        for (int group = 1; group <= g; group++) {
            System.out.print("-----------+");
        }
        System.out.println();

        // Print data
        for (int week = 1; week <= w; week++) {
            System.out.printf("| %-4d |", week);

            for (int group = 1; group <= g; group++) {
                System.out.print(" ");

                List<Integer> players = result[week][group];
                if (players != null) {
                    String playerList = players.stream().map(Object::toString).collect(Collectors.joining(", "));
                    System.out.printf("%-10s", playerList);
                } else {
                    System.out.print("           ");
                }

                System.out.print("|");
            }
            System.out.println();
        }

        // Print bottom border
        System.out.print("+------+");
        for (int group = 1; group <= g; group++) {
            System.out.print("-----------+");
        }
        System.out.println();
    }
    

    static void changeTimeBudget() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.print("\nEnter new time limit in seconds (current: " + timeBudget + "s): ");
                int timeBud = Integer.parseInt(scanner.nextLine());
                if (timeBud < 0) {
                    timeBud = 0;
                } else if (timeBud > 999999) {
                    timeBud = 999999;
                }
                timeBudget = timeBud;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid value\n");
                continue;
            }
            break;
        }
    }

    static void mainProgram() {
        // Đọc input từ file input.txt
        try {
            // Đọc input từ file input.txt
            File inputFile = new File("input.txt");
            Scanner fileScanner = new Scanner(inputFile);

            while (fileScanner.hasNext()) {
                // Đặt lại giá trị của các biến cốt lỗi
                allClauses.clear();
                satSolver = null;
                startTime = 0;

                // Đọc các giá trị từ file cho mỗi trường hợp
                w = fileScanner.nextInt();
                p = fileScanner.nextInt();
                g = fileScanner.nextInt();

                // Hiển thị thông tin từ input
                System.out.println("\nNumber of weeks: " + w);
                System.out.println("Number of players per group: " + p);
                System.out.println("Number of groups: " + g);

                // Gọi hàm giải quyết bài toán
                solveSatProblem();
            }

            // Đóng file scanner
            fileScanner.close();

        } catch (IOException e) {
            System.out.println("Error reading input from file: " + e.getMessage());
        }
    }
}