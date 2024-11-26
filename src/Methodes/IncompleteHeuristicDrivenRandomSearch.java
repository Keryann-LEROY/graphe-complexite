package Methodes;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import utils.ColRowGroupOccurenceVariableSelector;
import utils.SolutionData;
import utils.SolutionUnicityPropagator;
import utils.SudokuMetadata;

import java.time.Duration;
import java.util.*;

public class IncompleteHeuristicDrivenRandomSearch implements OptimalSudokuSolver{


    private Duration searchDuration;
    private Model model;
    private BoolVar[] assignmentvars;
    private IntVar nbClues;
    private SolutionUnicityPropagator solutionUnicityPropagator;
    private Constraint solutionUnicityConstraint;
    private Solver solver;
    private SudokuMetadata sudoku;
    private List<Integer> desiredSolution;

    private List<SolutionData> solutionsFound= new ArrayList<>();

    private SortedMap<Integer,List<List<Integer>>> foundSolutions=new TreeMap<>();

    public IncompleteHeuristicDrivenRandomSearch(SudokuMetadata sudoku, List<Integer> desiredSolution, Duration searchDuration) {
        this.searchDuration=searchDuration;
        this.sudoku = sudoku;
        this.desiredSolution = desiredSolution;
        resetModel();
    }

    public void resetModel(){
        //cré un model d'assiqnation de variables booléennes (autant que de variable dans le sudoku)
        model = new Model();
        assignmentvars = model.boolVarArray(sudoku.getNodes().size());

        // compte le nombre de false dans assignmentvars.(variable a maximiser)

        nbClues = model.intVar("nbClues", 0, assignmentvars.length);
        model.count(1, assignmentvars, nbClues).post();

        // definition de la contrainte d'unicité de la solution du sudoku,
        solutionUnicityPropagator = new SolutionUnicityPropagator(desiredSolution, sudoku, assignmentvars,false,false);
        solutionUnicityConstraint = new Constraint("uniqueSolutionSudoku", solutionUnicityPropagator);
        model.post(solutionUnicityConstraint);


        solver = model.getSolver();

        solver.setSearch(Search.intVarSearch(new ColRowGroupOccurenceVariableSelector(sudoku,assignmentvars,1,System.nanoTime()), new IntDomainMax(), assignmentvars));
    }

    public List<Integer> solve() {
        // resoudre pour le nombre d'indice minimal.
        List<Integer> solution = new ArrayList<>();
        long Start = System.nanoTime();
        long End = System.nanoTime();
        double Duration = (End - Start) * Math.pow(10, -9);
        long ExpirationDate = System.nanoTime() + searchDuration.toNanos();

        int minFound=assignmentvars.length;
        resetModel();
        while (System.nanoTime() < ExpirationDate) {
            resetModel();
            solver.solve();
            if(nbClues.getValue()<minFound) {
                minFound=nbClues.getValue();
                solution = solutionUnicityPropagator.applyMask(desiredSolution, assignmentvars);

                End = System.nanoTime();
                Duration = (End - Start) * Math.pow(10, -9);
                System.out.println("NbClues = " + nbClues + " - Elapsed Time: " + Duration);


                solutionsFound.add(new SolutionData(nbClues.getValue(), solution, Duration, sudoku, desiredSolution));
            }
        }

        solutionsFound.sort(Comparator.comparingInt(SolutionData::getNbClues));
        solution = solutionsFound.getFirst().getSolution();

        End = System.nanoTime();
        Duration = (End - Start) * Math.pow(10, -9);

        for(SolutionData data : solutionsFound){
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
        searchDuration=timeLimit;
    }
}
