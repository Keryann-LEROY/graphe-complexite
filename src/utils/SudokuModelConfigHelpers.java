package utils;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SudokuModelConfigHelpers {


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

//    public static Model configureSudoku(utils.SudokuMetadata sudoku, List<IntVar> generatedVars, List<Integer> values, BoolVar[] mask){
//
//        return configureSudoku(sudoku,generatedVars,applyMask(values,mask));
//    }

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
}
