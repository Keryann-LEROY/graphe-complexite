import Methodes.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import utils.SudokuMetadata;
import utils.SudokuModelConfigHelpers;

import java.util.*;

public class MinimalGridGenerator {


    public static void main(String[] args) {

        // Trouve une solution initial par recherche aléatoire. (contraintes de structure sans contraintes d'indices )
        SudokuMetadata sudoku = new SudokuMetadata(5,1);
        List<IntVar> initialSolutionVars = new ArrayList<>();
        Model initialModel = SudokuModelConfigHelpers.configureSudoku(sudoku,initialSolutionVars);
        Solver initialSolver = initialModel.getSolver();

        IntVar[] initialSolutionVarsArray = new IntVar[initialSolutionVars.size()];
        for (int i = 0; i < initialSolutionVars.size(); i++) {
            initialSolutionVarsArray[i] = initialSolutionVars.get(i);
        }
        initialSolver.setSearch(Search.randomSearch(initialSolutionVarsArray,System.currentTimeMillis()));

        long initialSolutionStart = System.nanoTime();
        initialSolver.solve();
        long initialSolutionEnd = System.nanoTime();

        double initialSolutionDuration = (initialSolutionEnd - initialSolutionStart)* Math.pow(10,-9);

        List<Integer> desiredSolution = new ArrayList<>();
        for (IntVar var : initialSolutionVars){
            desiredSolution.add(var.getValue());
        }

        System.out.println("Initial Solution:");
        System.out.println(sudoku.arrange(desiredSolution,2,'.'));
        System.out.println("Elapsed Time: "+initialSolutionDuration);

        //set the resolution methode
        // OptimalSudokuSolver solver = new CompleteReverseTreeExploration(sudoku,desiredSolution);
        // OptimalSudokuSolver  solver = new CompleteIcrementalSearch(sudoku,desiredSolution);
        OptimalSudokuSolver solver = new LargeNeighborhoodSearch(sudoku, desiredSolution);

        //use the resolution methode
        List<Integer> solution = solver.solve();

        List<IntVar> validationVars = new ArrayList<>();
        Model validationModel = SudokuModelConfigHelpers.configureSudoku(sudoku,validationVars,solution);
        Solver validationSolver = validationModel.getSolver();

        IntVar[] validationVarsArray = new IntVar[validationVars.size()];
        for (int i = 0; i < initialSolutionVars.size(); i++) {
            validationVarsArray[i] = validationVars.get(i);
        }
        validationSolver.setSearch(Search.randomSearch(validationVarsArray,System.currentTimeMillis()));

        System.out.println("Has One Solution: "+ validationSolver.solve());
        System.out.println("Has Two Solution: "+ validationSolver.solve());

    }
}