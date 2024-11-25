package Methodes;

import utils.SolutionData;

import java.time.Duration;
import java.util.List;

public interface OptimalSudokuSolver {
    public List<Integer> solve();
    public  List<SolutionData> getSolutionData();
    public void SetTimeLimit(Duration timeLimit);
}
