import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.ArrayList;
import java.util.List;

public class SolutionUnicityPropagator extends Propagator<BoolVar> {


    private List<Integer> desiredSolution;
    private SudokuMetadata sudoku;

    private BoolVar[] assignmentVars;
    private boolean undecPlaceholderIs1;
    private boolean onCompleteAssignment;


    public SolutionUnicityPropagator(List<Integer> desiredSolution, SudokuMetadata sudoku, BoolVar[] assignmentVars,boolean undecPlaceholderIs1,boolean onCompleteAssignment) {
        super(assignmentVars, PropagatorPriority.VERY_SLOW, false);
        this.desiredSolution = desiredSolution;
        this.sudoku = sudoku;
        this.assignmentVars = assignmentVars;
        this.undecPlaceholderIs1=undecPlaceholderIs1;
        this.onCompleteAssignment=onCompleteAssignment;
    }


    @Override
    public void propagate(int i) throws ContradictionException {
        boolean completeAssignment = true;
        if(onCompleteAssignment){
            for (BoolVar assignmentVar : assignmentVars) {
                if (!assignmentVar.isInstantiated())
                    completeAssignment=false;
            }
        }
        if(completeAssignment) {
            List<IntVar> vars = new ArrayList<>();
            Model model = MinimalGridGenerator.configureSudoku(sudoku, vars, applyMask(desiredSolution, assignmentVars));
            Solver solver = model.getSolver();
            //cherche une premiere solution.
            solver.solve();
            // si le solveur trouve une deuxieme solution, on renvoi une contradiction.
            if (solver.solve()) {
                fails();
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
            else if(mask[i].isInstantiatedTo(1) || undecPlaceholderIs1)
                newValues.add(values.get(i));
            else
                newValues.add(-1);
        }
        return newValues;
    }
}
