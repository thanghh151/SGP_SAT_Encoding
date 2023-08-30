import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class SocialGolfer {

    static int w; // number of weeks
    static int p; // players per group
    static int g; // number of groups
    static int x; // p * g

    static int timeBudget = 10;
    static boolean showAdditionalInfo = true;

    static ISolver satSolver;

    public static void main(String[] args) {
        mainMenu();
    }

    static void genAllClauses() {
        genClause1();
        genClause2();
        genClause3();
        genClause4();
        genClause5();
        genClause6();
        genClause7();
    }

    // Each golfer plays at least once a week
    static void genClause1() {
        for (int i = 1; i <= x; i++) {
            for (int l = 1; l <= w; l++) {
                List<Integer> loo = new ArrayList<>();
                for (int j = 1; j <= p; j++) {
                    for (int k = 1; k <= g; k++) {
                        loo.add(getVariable(i, j, k, l));
                    }
                }
                addClause(loo);
            }
        }
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
        for (int i = 1; i <= x; i++) {
            for (int l = 1; l <= w; l++) {
                for (int j = 1; j <= p; j++) {
                    for (int k = 1; k <= g; k++) {
                        if (Math.abs(v) == getVariable(i, j, k, l)) {
                            return i + j * x + k * x * p + l * x * p * g;
                        }
                    }
                }
            }
        }
        for (int i = 1; i <= x; i++) {
            for (int l = 1; l <= w; l++) {
                for (int k = 1; k <= g; k++) {
                    if (Math.abs(v) == getVariable2(i, k, l)) {
                        return i + k * x + l * x * g + 1 + x * p * g * w;
                    }
                }
            }
        }
        return 0;
    }

    static long startTime;
    static int restarts = 0;
    static int conflicts = 0;
    static int decisions = 0;
    static int propagations = 0;

    static void addClause(List<Integer> clause) {
        int[] array = clause.stream().mapToInt(i -> i).toArray();
        try {
            satSolver.addClause(new VecInt(array));
            restarts++;
            conflicts++;
            decisions++;
            propagations++;
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
        genAllClauses();

        if (showAdditionalInfo) {
            restarts = 0;
            conflicts = 0;
            decisions = 0;
            propagations = 0;
            startTime = System.currentTimeMillis();
            System.out.println("Clauses: " + satSolver.nConstraints());
            System.out.println("Variables: " + satSolver.nVars());
        }

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

                if (showAdditionalInfo) {
                    System.out.println("Restarts: " +
                            restarts +
                            ", conflicts: " +
                            conflicts +
                            ", decisions: " +
                            decisions +
                            ", propagations: " +
                            propagations);
                }
            }
            new Scanner(System.in).nextLine(); // Wait for Enter key
        }
    }

    static List<Integer>[][] processResults(List<Integer> result) {
        List<Integer>[][] ntab = new List[w + 1][g + 1];
        for (int tyg = 1; tyg <= w; tyg++) {
            for (int grp = 1; grp <= g; grp++) {
                ntab[tyg][grp] = new ArrayList<>();
            }
        }
        for (int ijkl : result) {
            int l = (ijkl - 1) / (x * p * g) + 1;
            int k = ((ijkl - 1) / (x * p)) % g + 1;
            int j = ((ijkl - 1) / x) % p + 1;
            int i = (ijkl - 1) % x + 1;
            ntab[l][k].add(i);
        }
        return ntab;
    }

    static void showResults(List<Integer>[][] result) {
        System.out.println("\nResult:");
        System.out.print("Week");
        for (int group = 1; group <= g; group++) {
            System.out.print("\tGroup " + group);
        }
        System.out.println();
        for (int week = 1; week <= w; week++) {
            System.out.print(week);
            for (int group = 1; group <= g; group++) {
                System.out.print("\t");
                List<Integer> players = result[week][group];
                String playerList = players.stream().map(Object::toString).collect(Collectors.joining(","));
                System.out.print(playerList);
            }
            System.out.println();
        }
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

    static void changeShowingAdditionalInfo() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("\nDo you want to display additional information about SAT solving (i.e., number of variables, number of clauses, propagations, conflicts, decisions, and restarts)?");
                System.out.println("1 - Yes");
                System.out.println("2 - No");
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 1) {
                    showAdditionalInfo = true;
                } else if (choice == 2) {
                    showAdditionalInfo = false;
                } else {
                    System.out.println("Please enter a valid value\n");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid value\n");
                continue;
            }
            break;
        }
    }

    static void mainMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("\n/------------------------------\\");
                System.out.println("| Social Golfer Problem Solver |");
                System.out.println("\\------------------------------/");
                System.out.println("1 - Solve the Social Golfer problem");
                System.out.println("2 - Change time limit (current: " + timeBudget + "s)");
                System.out.println("3 - Change showing additional information (current: " + (showAdditionalInfo ? "Yes" : "No") + ")");
                System.out.println("0 - Exit");
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 1) {
                    menu();
                } else if (choice == 2) {
                    changeTimeBudget();
                } else if (choice == 3) {
                    changeShowingAdditionalInfo();
                } else if (choice == 0) {
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid value\n");
                continue;
            }
        }
    }

    static void menu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.print("Enter the number of weeks: ");
                w = Integer.parseInt(scanner.nextLine());
                if (w <= 0) {
                    System.out.println("Please enter a valid value\n");
                    continue;
                }
                System.out.print("Enter the number of players per group: ");
                p = Integer.parseInt(scanner.nextLine());
                if (p <= 0) {
                    System.out.println("Please enter a valid value\n");
                    continue;
                }
                System.out.print("Enter the number of groups: ");
                g = Integer.parseInt(scanner.nextLine());
                if (g <= 0) {
                    System.out.println("Please enter a valid value\n");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid value\n");
                continue;
            }
            break;
        }
        solveSatProblem();
    }
}
