package utils;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import utils.SudokuMetadata;

import java.util.ArrayList;
import java.util.List;

public class SolutionUnicityPropagator extends Propagator<BoolVar> {


    private List<Integer> desiredSolution;
    private SudokuMetadata sudoku;

    private BoolVar[] assignmentVars;
    private boolean undecPlaceholderIs1;
    private boolean failureDriven;


    public SolutionUnicityPropagator(List<Integer> desiredSolution, SudokuMetadata sudoku, BoolVar[] assignmentVars, boolean undecPlaceholderIs1, boolean failureDriven) {
        super(assignmentVars, PropagatorPriority.VERY_SLOW, true);
        this.desiredSolution = desiredSolution;
        this.sudoku = sudoku;
        this.assignmentVars = assignmentVars;
        this.undecPlaceholderIs1 = undecPlaceholderIs1;
        this.failureDriven = failureDriven;
    }


    @Override
    public void propagate(int i) throws ContradictionException {
        List<IntVar> vars = new ArrayList<>();
        Model model = SudokuModelConfigHelpers.configureSudoku(sudoku, vars, applyMask(desiredSolution, assignmentVars));
        Solver solver = model.getSolver();
        if (failureDriven) {
            //cherche une premiere solution.
            solver.solve();
            // si le solveur trouve une deuxieme solution, on renvoi une contradiction.
            if (solver.solve()) {
                fails();
            }
        } else if (solver.solve()) {        // si on trouve une solution,
            if (!solver.solve()) {          // et pas une deuxième,
                filterForSuccess();     // alors le reste de l'assignation est trivial et peut etre directement affecter.
            }
        } else fails();
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        if (!assignmentVars[i].isInstantiatedTo(undecPlaceholderIs1 ? 1 : 0)) { // si la variable assignée est la valeur par default pas besoin de tester
            if (failureDriven) {
                failureDrivenTest();
            } else {
                successDrivenTest();
            }
        }

        boolean completeAssignment = true;
        for (BoolVar assignmentVar : assignmentVars) {
            if (!assignmentVar.isInstantiated())
                completeAssignment = false;
        }
        if(completeAssignment){ // si toutes les variable sont assignée force le test.
            failureDrivenTest();
        }
    }

    private void failureDrivenTest() throws ContradictionException {
        List<IntVar> vars = new ArrayList<>();
        Model model = SudokuModelConfigHelpers.configureSudoku(sudoku, vars, applyMask(desiredSolution, assignmentVars));
        Solver solver = model.getSolver();
        //cherche une premiere solution.
        solver.solve();
        // si le solveur trouve une deuxieme solution, on renvoi une contradiction.
        if (solver.solve()) {
            fails();
        }
    }

    private void successDrivenTest() throws ContradictionException {
        List<IntVar> vars = new ArrayList<>();
        Model model = SudokuModelConfigHelpers.configureSudoku(sudoku, vars, applyMask(desiredSolution, assignmentVars));
        Solver solver = model.getSolver();
        if (solver.solve()) {           // si on trouve une solution,
            if (!solver.solve()) {      // et pas une deuxième,
                filterForSuccess();     // alors le reste de l'assignation est trivial et peut etre directement affecter.
            }
        } else fails();
    }

    private void filterForSuccess() throws ContradictionException {
        for (BoolVar assignmentVar : assignmentVars) {
            if (!assignmentVar.isInstantiated()) {
                if (undecPlaceholderIs1) assignmentVar.setToTrue(new ICause() {
                });
                else assignmentVar.setToFalse(new ICause() {
                });
            }

        }
    }

    @Override
    public ESat isEntailed() {
        ESat retour = ESat.UNDEFINED;
        return retour;
    }

    public List<Integer> applyMask(List<Integer> values, BoolVar[] mask) {
        List<Integer> newValues = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            if (mask[i].isInstantiatedTo(0))
                newValues.add(-1);
            else if (mask[i].isInstantiatedTo(1) || undecPlaceholderIs1)
                newValues.add(values.get(i));
            else
                newValues.add(-1);
        }
        return newValues;
    }
}
