package Methodes;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import utils.SolutionUnicityPropagator;
import utils.SudokuMetadata;

import java.util.ArrayList;
import java.util.List;

public class CompleteIcrementalSearch implements OptimalSudokuSolver {

    private Model model;
    private BoolVar[] assignmentvars;
    private IntVar nbClues;
    private SolutionUnicityPropagator solutionUnicityPropagator;
    private Constraint solutionUnicityConstraint;
    private Solver solver;
    private SudokuMetadata sudoku;
    private List<Integer> desiredSolution;

    public CompleteIcrementalSearch(SudokuMetadata sudoku, List<Integer> desiredSolution) {

        this.sudoku = sudoku;
        this.desiredSolution = desiredSolution;
    }

    public void resetModel(int maxClues){
        //cré un model d'assiqnation de variables booléennes (autant que de variable dans le sudoku)
        model = new Model();
        assignmentvars = model.boolVarArray(sudoku.getNodes().size());

        // compte le nombre de false dans assignmentvars.(variable a maximiser)

        nbClues = model.intVar("nbClues", 0, assignmentvars.length);
        model.count(1, assignmentvars, nbClues).post();
        model.arithm(nbClues, "<=", maxClues).post();
        model.arithm(nbClues, ">=", maxClues).post();

        // definition de la contrainte d'unicité de la solution du sudoku,
        solutionUnicityPropagator = new SolutionUnicityPropagator(desiredSolution, sudoku, assignmentvars,false,false);
        solutionUnicityConstraint = new Constraint("uniqueSolutionSudoku", solutionUnicityPropagator);
        model.post(solutionUnicityConstraint);


        solver = model.getSolver();

        solver.setSearch(Search.intVarSearch(new Random<IntVar>(System.nanoTime()), new IntDomainMax(), assignmentvars));
    }

    public List<Integer> solve() {
        // resoudre pour le nombre d'indice minimal.
        List<Integer> solution = new ArrayList<>();
        long Start = System.nanoTime();
        long End = System.nanoTime();
        double Duration = (End - Start) * Math.pow(10, -9);

        int maxClues = 0;

        do {
            maxClues++;
            End = System.nanoTime();
            Duration = (End - Start) * Math.pow(10, -9);
            System.out.println("Test maxClues = "+maxClues +" - Elapsed Time: " + Duration);
            resetModel(maxClues);

        }while (maxClues <= assignmentvars.length && !solver.solve()); // retoune true si le solveur a trouver une solution meilleur que toutes les precedentes. false

        solution = solutionUnicityPropagator.applyMask(desiredSolution, assignmentvars);
        int nbFixed = 0;
        int nbFree = 0;
        int nbUndec = 0;
        for (int i = 0; i < assignmentvars.length; i++) {
            if (!assignmentvars[i].isInstantiated())
                nbUndec++;
            else if (assignmentvars[i].isInstantiatedTo(0))
                nbFree++;
            else if (assignmentvars[i].isInstantiatedTo(1))
                nbFixed++;
        }
        System.out.println();
        System.out.println("nbBlank: " + nbClues.getValue());
        System.out.println("nbFixed: " + nbFixed);
        System.out.println("nbFree: " + nbFree);
        System.out.println("nbUndec: " + nbUndec);
        System.out.println(sudoku.arrange(solution, 2, '.'));

        End = System.nanoTime();
        Duration = (End - Start) * Math.pow(10, -9);

        System.out.println("Solution:");
        System.out.println(sudoku.arrange(solution, 2, '.'));
        System.out.println("Elapsed Time: " + Duration);

        return solution;
    }
}
