import java.util.*;

public class SudokuMetadata {

    private int size ;
    private int a;
    private int b;
    private List<Integer> nodes = new ArrayList<>();
    private Set<Edge> edges = new HashSet<>();
    private int[] domain;
    private List<String> names =new ArrayList<>();
    private Map<Integer,Set<Integer>> cols = new HashMap<>();
    private Map<Integer,Set<Integer>> rows = new HashMap<>();
    private Map<Integer,Set<Integer>> groups = new HashMap<>();

    /**
     * Construit une grille de sudoku avec des cotés de longueur a*b.
     *
     *
     *  ex: a=2 ; b=3 :
     *   0  0  0 | 1  1  1
     *   0  0  0 | 1  1  1
     *   ----------------
     *   2  2  2 | 3  3  3
     *   2  2  2 | 3  3  3
     *   ----------------
     *   4  4  4 | 5  5  5
     *   4  4  4 | 5  5  5
     *
     * @param a  subdivision axe x
     * @param b  subdivision axe y
     */
    public SudokuMetadata(int a , int b) {
        this.a=a;
        this.b=b;
        this.size = a*b;
        for (int i=0;i<size*size;i++){
            nodes.add(i);
            names.add("v"+(i%size)+","+(i/size));
        }
        domain = new int[size];
        for (int i=0;i<size;i++){
            domain[i]=i;
            cols.put(i,new HashSet<>());
            rows.put(i,new HashSet<>());
            groups.put(i,new HashSet<>());
        }
        for(int i : nodes){
            for(int j : nodes){
                if(i!=j){
                    int xi = i%size;
                    int yi = i/size;
                    int xj = j%size;
                    int yj = j/size;

                    if (xi == xj){
                        edges.add(new Edge(i,j));
                    }

                    if (yi == yj){
                        edges.add(new Edge(i,j));
                    }
                    if (xi/a == xj/a && yi/b == yj/b){
                        edges.add(new Edge(i,j));
                    }
                }
            }
            int x = i%size;
            int y = i/size;
            cols.get(x).add(i);
            rows.get(y).add(i);
            groups.get((y/a)*a + x/b).add(i);


        }


    }

    public int getSize() {
        return size;
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    public List<Integer> getNodes() {
        return nodes;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public int[] getDomain() {
        return domain;
    }

    public List<String> getNames() {
        return names;
    }

    public Map<Integer, Set<Integer>> getCols() {
        return cols;
    }

    public Map<Integer, Set<Integer>> getRows() {
        return rows;
    }

    public Map<Integer, Set<Integer>> getGroups() {
        return groups;
    }


    public String arrange(List<Integer> values, int nbchar){
        String out = "";
        String hsep = "";
        String bdash = "|-";
        String fdash = "";
        for(int  i =0; i<b;i++){
            bdash+="-";
        }
        for(int  i =0; i<nbchar;i++){
            fdash+="-";
        }
        for(int  i =0; i<a*b;i++) {
            if (i % b==0) {
                hsep += bdash;
            }
            hsep += fdash;
        }
        hsep += "|";

        for (int i = 0; i <values.size() ; i++) {
            if (((i)%(b*a))==0 && i!=0){
                out+="|\n";
            }
            if (((i)%(b*a*a))==0) {
                out += hsep + "\n";
            }
            if ((i%b)==0) {
                out += "| ";
            }

            out+=String.format("%"+nbchar+"d ",values.get(i));
        }
        out+="|"+"\n"+hsep;
        return out;
    }

    public String arrange(List<Integer> values, int nbchar,char m1Placeholder){
        String out = arrange(values,nbchar);
        out = out.replace("-1"," "+m1Placeholder);
        return out;
    }

    public class Edge{
        private int vertex1;
        private int vertex2;

        public Edge(int vertex1, int vertex2) {
            this.vertex1 = vertex1;
            this.vertex2 = vertex2;
        }

        public int getVertex1() {
            return vertex1;
        }

        public int getVertex2() {
            return vertex2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return vertex1 == edge.vertex1 && vertex2 == edge.vertex2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(vertex1, vertex2);
        }
    }
}
