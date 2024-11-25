import Methodes.CompleteIcrementalSearch;
import Methodes.CompleteReverseTreeExploration;
import Methodes.IncompleteHeuristicDrivenRandomSearch;
import Methodes.OptimalSudokuSolver;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.mis.F;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import utils.SolutionData;
import utils.SudokuMetadata;
import utils.SudokuModelConfigHelpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SudokuBenchmark {

    public static void test(int a, int b,Duration timelimit) throws IOException {
        // Trouve une solution initial par recherche aléatoire. (contraintes de structure sans contraintes d'indices )
        SudokuMetadata sudoku = new SudokuMetadata(a,b);
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


        BufferedWriter csvOut;
        File file = new File("results_"+a+"_"+b+".csv");
        if(file.createNewFile()){
            // création et ouverture du fichier si il n'existe pas
            csvOut = new BufferedWriter(new FileWriter(file,true));

            // ajout des entête de colonnes.
            SolutionData s = new SolutionData(-1,desiredSolution,-1,sudoku,desiredSolution);
            csvOut.write(s.colNamesCSV());
            csvOut.newLine();
        }else {
            // ouverture du fichier
            csvOut = new BufferedWriter(new FileWriter(file,true));
        }


        List<OptimalSudokuSolver> methodes = new ArrayList<>();

        //set the resolution methode

        // CompleteReverseTreeExploration
        //methodes.add( new CompleteReverseTreeExploration(sudoku,desiredSolution));

        // CompleteReverseTreeExploration with time limit
        OptimalSudokuSolver methode = new CompleteReverseTreeExploration(sudoku,desiredSolution);
        methode.SetTimeLimit(timelimit);
        methodes.add( methode);

        //CompleteIcrementalSearch
        //methodes.add( new CompleteIcrementalSearch(sudoku,desiredSolution));

        //IncompleteHeuristicDrivenRandomSearch
        methodes.add(new IncompleteHeuristicDrivenRandomSearch(sudoku,desiredSolution, timelimit));



        //use the resolution methode
        for (int i = 0; i < methodes.size(); i++) {
            methodes.get(i).solve();

            for (SolutionData solutionData : methodes.get(i).getSolutionData()){
                // ajoute l'id de la methode
                solutionData.setMethodeId(i);
                //ecrit une entré dans le fichier de sortie

                csvOut.write(solutionData.toCSV());
                csvOut.newLine();
            }
        }



        csvOut.close();
    }

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 20; i++) {
            test(3,2,Duration.ofSeconds(10));
        }




    }
}