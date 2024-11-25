package utils;

import java.util.List;

public class SolutionData {

    private String gridId;
    
    private int methodeId;

    private int nbClues;

    private double timeFound;
    private double timeEnd=-1;

    private List<Integer> solution;

    public SolutionData(int nbClues, List<Integer> solution, double timeFound, SudokuMetadata sudokuMetadata, List<Integer> desiredSolution) {
        this.nbClues = nbClues;
        this.solution = solution;
        this.timeFound = timeFound;
        setGridId(sudokuMetadata,desiredSolution);
    }

    public String getGridId() {
        return gridId;
    }

    public void setGridId(SudokuMetadata sudokuMetadata,List<Integer> desiredSolution) {
        StringBuilder newid = new StringBuilder(sudokuMetadata.getA() + "_" + sudokuMetadata.getB() + ":");
        for (int val : desiredSolution){
            newid.append(val);
        }
        this.gridId =newid.substring(0) ;
    }

    public int getNbClues() {
        return nbClues;
    }

    public List<Integer> getSolution() {
        return solution;
    }

    public double getTimeFound() {
        return timeFound;
    }

    public double getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(double timeEnd) {
        this.timeEnd = timeEnd;
    }

    public int getMethodeId() {
        return methodeId;
    }

    public void setMethodeId(int methodeId) {
        this.methodeId = methodeId;
    }

    public void setNbClues(int nbClues) {
        this.nbClues = nbClues;
    }

    public void setTimeFound(double timeFound) {
        this.timeFound = timeFound;
    }

    public void setSolution(List<Integer> solution) {
        this.solution = solution;
    }

    public String toCSV(){
        StringBuilder line = new StringBuilder();
        line.append(gridId).append(";");
        line.append(methodeId).append(";");
        line.append(nbClues).append(";");
        line.append(timeFound).append(";");
        line.append(timeEnd).append(";");
        for (int i : solution) {
            line.append(i).append(";");
        }
        line.append(solution.size());
        return line.substring(0);
    }

    public String colNamesCSV(){
        StringBuilder line = new StringBuilder();
        line.append("gridId").append(";");
        line.append("methodeId").append(";");
        line.append("nbClues").append(";");
        line.append("timeFound").append(";");
        line.append("timeEnd").append(";");
        for (int i =0;i<solution.size();i++) {
            line.append("v_").append(i).append(";");
        }
        line.append(solution.size());
        return line.substring(0);
    }
}
