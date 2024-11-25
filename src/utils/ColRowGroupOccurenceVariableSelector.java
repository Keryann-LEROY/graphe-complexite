package utils;

import org.chocosolver.solver.search.strategy.selectors.variables.VariableEvaluator;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.*;
import java.util.random.RandomGenerator;

public class ColRowGroupOccurenceVariableSelector implements  VariableEvaluator<IntVar>, VariableSelector<IntVar> {


    private Map<BoolVar, List<BoolVar>> varLinks = new HashMap<>();
    private final int valueCounted;

    private RandomGenerator randomGenerator;

    public ColRowGroupOccurenceVariableSelector(SudokuMetadata sudoku,BoolVar[] vars,int valueCounted,long seed){
        this.randomGenerator = new Random(seed);
        this.valueCounted=valueCounted;
        for (int index : sudoku.getNodes()){
            varLinks.put(vars[index],new ArrayList<>());
        }
        for (SudokuMetadata.Edge edge : sudoku.getEdges()){
            varLinks.get(vars[edge.getVertex1()]).add(vars[edge.getVertex2()]);
        }
    }

    @Override
    public double evaluate(IntVar var) {
        double score = 0;
        for (BoolVar neighbor : varLinks.get(var)){
            if(neighbor.isInstantiatedTo(valueCounted))score++;
        }
        return score;
    }

    @Override
    public IntVar getVariable(IntVar[] vars) {
        List<IntVar> boolVars = new ArrayList<>();
        for(IntVar var :vars){
            if(!var.isInstantiated()) boolVars.add(var);
        }
        Collections.shuffle(boolVars,randomGenerator);
        Map<IntVar, Double> varscores = new HashMap<>();
        for (IntVar var : boolVars){
            varscores.put(var,evaluate(var));
        }
        boolVars.sort(Comparator.comparingDouble(varscores::get));
        return boolVars.isEmpty()?null:boolVars.getFirst();
    }
}
