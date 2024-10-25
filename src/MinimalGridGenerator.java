import jdk.jfr.Timespan;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.nio.channels.Selector;
import java.util.*;

public class MinimalGridGenerator {

    public static List<Integer> applyMask(List<Integer> values, BoolVar[] mask){
        List<Integer> newValues=new ArrayList<>();
        for (int i = 0; i <values.size() ; i++) {
            if(mask[i].isInstantiatedTo(0))
                newValues.add(-1);
            else
                newValues.add(values.get(i));
        }
        return newValues;
    }

    public static Model configureSudoku(SudokuMetadata sudoku, List<IntVar> generatedVars, List<Integer> values, BoolVar[] mask){

        return configureSudoku(sudoku,generatedVars,applyMask(values,mask));
    }

    public static Model configureSudoku(SudokuMetadata sudoku, List<IntVar> generatedVars){
        List<Integer> values=new ArrayList<>();
        for (int i : sudoku.getNodes()){
            values.add(-1);
        }
        return configureSudoku(sudoku,generatedVars,values);
    }
    public static Model configureSudoku(SudokuMetadata sudoku, List<IntVar> generatedVars,List<Integer> values){

        Model model = new  Model();
        for (int i : sudoku.getNodes()) {
            if (values.get(i) == -1) {
                generatedVars.add(model.intVar(sudoku.getNames().get(i), sudoku.getDomain()));
            } else {
                generatedVars.add(model.intVar(sudoku.getNames().get(i), values.get(i)));
            }
        }


        makeAllDiffs(model,generatedVars,sudoku.getRows());
        makeAllDiffs(model,generatedVars,sudoku.getCols());
        makeAllDiffs(model,generatedVars,sudoku.getGroups());

        return model;
    }

    public static void makeAllDiffs(Model model, List<IntVar> vars, Map<Integer, Set<Integer>> varGroups){
        varGroups.forEach((key, indexes) -> {
            IntVar[] groupVars = new IntVar[indexes.size()];
            int i=0;
            for (int idx : indexes) {
                groupVars[i]= vars.get(idx);
                i++;
            }
            model.allDifferent(groupVars).post();
        });
    }

    public static void main(String[] args) {

        // Trouve une solution initial par recherche aléatoire. (contraintes de structure sans contraintes d'indices )
        SudokuMetadata sudoku = new SudokuMetadata(3,2);
        List<IntVar> initialSolutionVars = new ArrayList<>();
        Model initialModel = configureSudoku(sudoku,initialSolutionVars);
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


        //cré un model d'assiqnation de variables booléennes (autant que de variable dans le sudoku)
        Model nbBlankMaximizationModel = new Model();
        BoolVar[] assignmentvars = nbBlankMaximizationModel.boolVarArray(sudoku.getNodes().size());

        // compte le nombre de false dans assignmentvars.(variable a maximiser)

        IntVar nbBlank = nbBlankMaximizationModel.intVar("nbBlank", 0, assignmentvars.length);
        nbBlankMaximizationModel.count(0,assignmentvars,nbBlank).post();
        nbBlankMaximizationModel.setObjective(true,nbBlank);

        // definition de la contrainte d'unicité de la solution du sudoku,
        Propagator<BoolVar> solutionUnicityPropagator = new SolutionUnicityPropagator(desiredSolution,sudoku,assignmentvars);
        Constraint solutionUnicityConstraint = new Constraint("uniqueSolutionSudoku", solutionUnicityPropagator);
        nbBlankMaximizationModel.post(solutionUnicityConstraint);



        Solver solver = nbBlankMaximizationModel.getSolver();

        solver.setSearch(Search.intVarSearch(new Random<IntVar>(System.nanoTime()), new IntDomainMin(),assignmentvars));


        // resoudre pour le nombre d'indice minimal.
        List<Integer> solution=new ArrayList<>();
        long Start = System.nanoTime();
        long End = System.nanoTime();
        double Duration = (End - Start)* Math.pow(10,-9);
        while (solver.solve()){
            solution = applyMask(desiredSolution,assignmentvars);
            int nbFixed =0;
            int nbFree =0;
            int nbUndec =0;
            for (int i = 0; i < assignmentvars.length; i++) {
                if(!assignmentvars[i].isInstantiated())
                    nbUndec++;
                else if (assignmentvars[i].isInstantiatedTo(0))
                    nbFree++;
                else if (assignmentvars[i].isInstantiatedTo(1))
                    nbFixed++;
            }
            System.out.println();
            System.out.println("nbBlank: "+nbBlank.getValue());
            System.out.println("nbFixed: "+nbFixed);
            System.out.println("nbFree: "+nbFree);
            System.out.println("nbUndec: "+nbUndec);
            System.out.println(sudoku.arrange(solution,2,'.'));
            End = System.nanoTime();
            Duration = (End - Start)* Math.pow(10,-9);
            System.out.println("Elapsed Time: "+Duration);
        }





        End = System.nanoTime();
        Duration = (End - Start)* Math.pow(10,-9);

        System.out.println("Solution:");
        System.out.println(sudoku.arrange(solution,2,'.'));
        System.out.println("Elapsed Time: "+Duration);

        //
        List<IntVar> validationVars = new ArrayList<>();
        Model validationModel = configureSudoku(sudoku,validationVars,solution);
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