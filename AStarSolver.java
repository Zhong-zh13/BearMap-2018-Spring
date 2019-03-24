import java.util.*;

/* This class helps to find out the shortest path using A*. Could have implement directly in the Router class,
*  but a new class makes it neater (I think)*/
public class AStarSolver {
    public LinkedList<Long> Result = new LinkedList<>();;
    public AStarSolver(GraphDB g, long start, long dest){
        HashMap<Long,Double> Distance = new HashMap<>();
        Distance.put(start,0.0);
        HashMap<Long,Long> prev = new HashMap<>();
        prev.put(start,start);
        PriorityQueue<Cell> pq = new PriorityQueue<>(10,new cellComp());
        pq.add(new Cell(start,0.0,g.distance(start,dest)));
        double inf = Double.MAX_VALUE/10;
        while(pq.size()>0){
            Cell cur = pq.poll();
            if(Distance.getOrDefault(cur.id,inf)<cur.coveredDist) continue;
            if(cur.id==dest) break;
            HashMap<Long,Double> neigh = g.NodeMap.get(cur.id).neighbours;
            for(Map.Entry<Long,Double> e:neigh.entrySet()){
                long to = e.getKey();
                double newDist = cur.coveredDist+e.getValue();
                if(Distance.getOrDefault(to,inf)>newDist){
                    Distance.put(to,newDist);
                    prev.put(to,cur.id);
                    pq.add(new Cell(to,newDist,g.distance(to,dest)));
                }
            }
        }
        long end = dest;
        while(end!=start){
            Result.addFirst(end);
            end = prev.get(end);
        }
        Result.addFirst(start);
        pq.clear();
    }
    LinkedList<Long> returnResult(){
        return Result;
    }
    class cellComp implements Comparator<Cell>{
        public int compare(Cell a, Cell b){
            return Double.compare(a.coveredDist+a.toDest,b.coveredDist+b.toDest);
        }
    }
    class Cell{
        long id;
        double coveredDist, toDest;
        public Cell(long i, double c, double t){
            id = i;
            coveredDist = c; toDest = t;
        }
    }
}
