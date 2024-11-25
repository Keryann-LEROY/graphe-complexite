package Methodes;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;

import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import utils.SolutionUnicityPropagator;
import utils.SudokuMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LargeNeighborhoodSearch implements OptimalSudokuSolver {

    private Model model;
    private BoolVar[] assignmentVars;
    private IntVar nbClues;
    private SolutionUnicityPropagator solutionUnicityPropagator;
    private Constraint solutionUnicityConstraint;
    private Solver solver;
    private SudokuMetadata sudoku;
    private List<Integer> desiredSolution;

    public LargeNeighborhoodSearch(SudokuMetadata sudoku, List<Integer> desiredSolution) {
        this.sudoku = sudoku;
        this.desiredSolution = desiredSolution;
    }

    private void resetModel(int maxClues, List<Integer> partialSolution) {
        model = new Model();
        assignmentVars = model.boolVarArray(sudoku.getNodes().size());

        nbClues = model.intVar("nbClues", 0, assignmentVars.length);
        model.count(1, assignmentVars, nbClues).post();
        model.arithm(nbClues, "<=", maxClues).post();
        model.arithm(nbClues, ">=", maxClues).post();

        solutionUnicityPropagator = new SolutionUnicityPropagator(partialSolution, sudoku, assignmentVars, false, false);
        solutionUnicityConstraint = new Constraint("uniqueSolutionSudoku", solutionUnicityPropagator);
        model.post(solutionUnicityConstraint);

        solver = model.getSolver();

        solver.setSearch(Search.intVarSearch(new org.chocosolver.solver.search.strategy.selectors.variables.Random<IntVar>(System.nanoTime()), new IntDomainMax(), assignmentVars));

    }

    private List<Integer> perturbSolution(List<Integer> solution, double perturbationRate) {
        List<Integer> newSolution = new ArrayList<>(solution);
        Random random = new Random();

        for (int i = 0; i < newSolution.size(); i++) {
            if (random.nextDouble() < perturbationRate) {
                newSolution.set(i, -1);
            }
        }
        return newSolution;
    }

    @Override
    public List<Integer> solve() {
        List<Integer> solution = new ArrayList<>();
        long startTime = System.nanoTime();
        int maxClues = 10; // Initialisation avec un petit voisinage
        double perturbationRate = 0.3; // Taux de perturbation pour la diversification

        List<Integer> currentSolution = new ArrayList<>(desiredSolution);
        boolean solutionFound = false;

        while (!solutionFound && maxClues <= sudoku.getNodes().size()) {
            List<Integer> perturbedSolution = perturbSolution(currentSolution, perturbationRate);
            resetModel(maxClues, perturbedSolution);

            if (solver.solve()) {
                solution = solutionUnicityPropagator.applyMask(desiredSolution, assignmentVars);

                int nbFixed = 0, nbFree = 0, nbUndec = 0;
                for (BoolVar var : assignmentVars) {
                    if (!var.isInstantiated()) nbUndec++;
                    else if (var.isInstantiatedTo(1)) nbFixed++;
                    else nbFree++;
                }

                System.out.println("nbBlank: " + nbClues.getValue());
                System.out.println("nbFixed: " + nbFixed);
                System.out.println("nbFree: " + nbFree);
                System.out.println("nbUndec: " + nbUndec);
                System.out.println(sudoku.arrange(solution, 2, '.'));

                currentSolution = new ArrayList<>(solution);
                solutionFound = true;
            } else {
                maxClues++; // Augmente le voisinage si aucune solution n'est trouvée
            }
        }

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) * 1e-9;

        System.out.println("Elapsed Time: " + duration);
        return solution;
    }
}
