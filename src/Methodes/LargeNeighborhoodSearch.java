package Methodes;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.search.limits.TimeCounter;
import org.chocosolver.solver.search.loop.lns.INeighborFactory;
import org.chocosolver.solver.search.strategy.Search;

import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainRandom;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import utils.SolutionData;
import utils.SolutionUnicityPropagator;
import utils.SudokuMetadata;
import utils.SudokuNeighbors;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LargeNeighborhoodSearch implements OptimalSudokuSolver {

    private Model model;
    private BoolVar[] assignmentVars;
    private IntVar nbClues;
    private SolutionUnicityPropagator solutionUnicityPropagator;
    private Constraint solutionUnicityConstraint;
    private Solver solver;
    private SudokuMetadata sudoku;
    private List<Integer> desiredSolution;
    private List<SolutionData> solutionsFound = new ArrayList<>();

    private SudokuNeighbors neighbor;

    public LargeNeighborhoodSearch(SudokuMetadata sudoku, List<Integer> desiredSolution,Duration timeLimit) {
        this.sudoku = sudoku;
        this.desiredSolution = desiredSolution;
        resetModel();
        SetTimeLimit(timeLimit);
    }

    private void resetModel() {
        model = new Model();
        assignmentVars = model.boolVarArray(sudoku.getNodes().size());

        nbClues = model.intVar("nbClues", 0, assignmentVars.length);
        model.count(1, assignmentVars, nbClues).post();
        model.setObjective(false, nbClues);


        solutionUnicityPropagator = new SolutionUnicityPropagator(desiredSolution, sudoku, assignmentVars, true, true);
        solutionUnicityConstraint = new Constraint("uniqueSolutionSudoku", solutionUnicityPropagator);
        model.post(solutionUnicityConstraint);

        solver = model.getSolver();

        neighbor = new SudokuNeighbors(assignmentVars,0.95);
        //solver.setSearch(Search.intVarSearch(new Random<IntVar>(System.nanoTime()), new IntDomainRandom(System.nanoTime()),assignmentVars));
        solver.setLNS(INeighborFactory.random(assignmentVars), new FailCounter(solver, 100));
        solver.findOptimalSolution(nbClues, Model.MINIMIZE);

    }

//    private List<Integer> perturbSolution(List<Integer> solution, double perturbationRate) {
//        List<Integer> newSolution = new ArrayList<>(solution);
//        Random random = new Random();
//
//        for (int i = 0; i < newSolution.size(); i++) {
//            if (random.nextDouble() < perturbationRate) {
//                newSolution.set(i, -1);
//            }
//        }
//        return newSolution;
//    }

    @Override
    public List<Integer> solve() {
        // resoudre pour le nombre d'indice minimal.
        List<Integer> solution = new ArrayList<>();
        long Start = System.nanoTime();
        long End = System.nanoTime();
        double Duration = (End - Start) * Math.pow(10, -9);


        while (solver.solve() ) {
            solution = solutionUnicityPropagator.applyMask(desiredSolution, neighbor.);

            End = System.nanoTime();
            Duration = (End - Start) * Math.pow(10, -9);
            System.out.println("NbClues = " + nbClues + " - Elapsed Time: " + Duration);


            solutionsFound.add(new SolutionData(nbClues.getValue(), solution, Duration, sudoku, desiredSolution));
        }


        solutionsFound.sort(Comparator.comparingInt(SolutionData::getNbClues));
        solution = solutionsFound.getFirst().getSolution();

        End = System.nanoTime();
        Duration = (End - Start) * Math.pow(10, -9);

        for (SolutionData data : solutionsFound) {
            data.setTimeEnd(Duration);
        }
        System.out.println("Solution:");
        System.out.println(sudoku.arrange(solution, 2, '.'));
        System.out.println("Elapsed Time: " + Duration);


        return solution;
    }


    @Override
    public List<SolutionData> getSolutionData() {
        return solutionsFound;
    }

    @Override
    public void SetTimeLimit(Duration timeLimit) {
        solver.addStopCriterion(new TimeCounter(solver, timeLimit.toNanos()));
    }
}
