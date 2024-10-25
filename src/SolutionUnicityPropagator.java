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

    public SolutionUnicityPropagator(List<Integer> desiredSolution, SudokuMetadata sudoku, BoolVar[] assignmentVars) {
        super(assignmentVars, PropagatorPriority.VERY_SLOW,false);
        this.desiredSolution = desiredSolution;
        this.sudoku = sudoku;
        this.assignmentVars = assignmentVars;
    }



    @Override
    public void propagate(int i) throws ContradictionException {
        boolean completeAssignment= true;
        /*
        for (BoolVar var: assignmentVars){
            if(!var.isInstantiated())
                completeAssignment=false;
        }
         */
        if(completeAssignment) {
            List<IntVar> vars = new ArrayList<>();
            Model model = MinimalGridGenerator.configureSudoku(sudoku, vars, desiredSolution, assignmentVars);
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
        ESat retour=ESat.TRUE;
        List<IntVar> vars = new ArrayList<>();
        Model model = MinimalGridGenerator.configureSudoku(sudoku,vars,desiredSolution,assignmentVars);
        Solver solver= model.getSolver();
        if(!solver.solve()){
            retour=ESat.FALSE;
        }

        if(solver.solve()){
            retour=ESat.FALSE;
        }
        return retour;
    }
}
