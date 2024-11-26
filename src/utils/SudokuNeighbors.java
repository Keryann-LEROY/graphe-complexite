package utils;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.AbstractEventObserver;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

public class SudokuNeighbors implements INeighbor {
    private BoolVar[] assignmentVars;

    private int[] values;

    private List<SolutionData> solutionsFound = new ArrayList<>();
    private double fixingRate=0.995;

    public SudokuNeighbors(BoolVar[] assignmentVars, double fixingRate) {
        this.assignmentVars = assignmentVars;

        this.values = new int[assignmentVars.length];
        this.fixingRate = fixingRate;
    }

    @Override
    public void recordSolution() {
        for (int i = 0; i < assignmentVars.length; i++) {
            values[i] = assignmentVars[i].getValue();
        }
    }

    @Override
    public void fixSomeVariables() throws ContradictionException {
        for (int i = 0; i < assignmentVars.length; i++) {
            if(Math.random() < fixingRate){
                assignmentVars[i].instantiateTo(values[i], this);
                // alternatively call: `this.freeze(i);`
            }
        }
    }

    @Override
    public void restrictLess() {
        fixingRate*=0.95;
    }

    @Override
    public void loadFromSolution(Solution solution) {
        assignmentVars = solution.retrieveBoolVars().toArray(new BoolVar[0]);
        this.values = new int[assignmentVars.length];
        recordSolution();
    }

    @Override
    public boolean isSearchComplete(){
        return fixingRate<0.85;
    }


}
