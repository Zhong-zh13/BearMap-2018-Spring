import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    public static final double ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
            ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;

    private int Depth;
    private boolean Query_success;
    private String[][] Render_grid;
    private double Raster_ul_lon;
    private double Raster_ul_lat;
    private double Raster_lr_lon;
    private double Raster_lr_lat;

    private double LonDPP;


    public Rasterer() {
        // YOUR CODE HERE

    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        // System.out.println(params);
        Map<String, Object> results = new HashMap<>();
        //System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
        //                  + "your browser.");
        Query_success = queryValid(params);
        if(!Query_success){
            // Return arbitrary values
            results.put("query_success",Query_success);
            results.put("render_grid",0);
            results.put("depth",0);
            results.put("raster_ul_lon",0);
            results.put("raster_ul_lat",0);
            results.put("raster_lr_lon",0);
            results.put("raster_lr_lat",0);
            return results;
        }


        double ul_lon = params.get("ullon");
        double lr_lon = params.get("lrlon");
        double ul_lat = params.get("ullat");
        double lr_lat = params.get("lrlat");
        double w = params.get("w");

        if(ul_lon < ROOT_ULLON) ul_lon = ROOT_ULLON;
        if(ul_lat > ROOT_ULLAT) ul_lat = ROOT_ULLAT;
        if(lr_lon > ROOT_LRLON) lr_lon = ROOT_LRLON;
        if(lr_lat < ROOT_LRLAT) lr_lat = ROOT_LRLAT;

        LonDPP = getLonDPP(lr_lon, ul_lon, w);
        Depth = getDepth(LonDPP);
        int[][] BoundaryGrids = BoundaryGrids(Depth, ul_lon, lr_lon, ul_lat, lr_lat);
        double[][] RasterBoundary = GetRasterBoundary(Depth, BoundaryGrids);

        // In the form of double array {{raster_ul_lon,raster_ul_lat},{raster_lr_lon,raster_lr_lat}}
        Raster_ul_lon = RasterBoundary[0][0];
        Raster_ul_lat = RasterBoundary[0][1];
        Raster_lr_lon = RasterBoundary[1][0];
        Raster_lr_lat = RasterBoundary[1][1];
        Render_grid = GetImgFiles(BoundaryGrids, Depth);

        results.put("query_success",Query_success);
        results.put("render_grid",Render_grid);
        results.put("depth",Depth);
        results.put("raster_ul_lon",Raster_ul_lon);
        results.put("raster_ul_lat",Raster_ul_lat);
        results.put("raster_lr_lon",Raster_lr_lon);
        results.put("raster_lr_lat",Raster_lr_lat);


        return results;
    }

    // If input is valid, return true
    private boolean queryValid(Map<String, Double> params){
        double ul_lon = params.get("ullon");
        double lr_lon = params.get("lrlon");
        double ul_lat = params.get("ullat");
        double lr_lat = params.get("lrlat");
        double w = params.get("w");
        double h = params.get("h");
        // You can also imagine that the user might drag the query box to
        // a location that is completely outside of the root longitude/latitudes._____This means lrlon < ullon, etc
        if(!checkBoundary(ul_lon, lr_lon, ul_lat, lr_lat)) return false;
        if(!checkRelative(ul_lon, lr_lon, ul_lat, lr_lat)) return false;
        if(!checkWHValue(w,h)) return false;
        return true;
    }
    // Check whether the longs and lats are out of boundary of the map.
    private boolean checkBoundary(double ul_lon, double lr_lon, double ul_lat, double lr_lat){
        if(ul_lon>ROOT_LRLON || lr_lon < ROOT_ULLON || ul_lat < ROOT_LRLAT || lr_lat > ROOT_ULLAT ){
            return false;
        }
        return true;
    }

    // Check whether relative position is right
    private boolean checkRelative(double ul_lon, double lr_lon, double ul_lat, double lr_lat){
        return (ul_lon < lr_lon)&&(ul_lat > lr_lat);
    }
    // Check whether there is negative w or h
    private boolean checkWHValue(double w, double h){
        return (w > 0)&&(h > 0);
    }
    // Compute the LongDPP of the query
    private double getLonDPP(double lr_lon, double ul_lon,double w){
        return (lr_lon - ul_lon)/w;
    }

    // Compute the depth
    private int getDepth(double LonDPP){
        double largest_longDPP = (ROOT_LRLON - ROOT_ULLON)/256;
        //double temp = largest_longDPP/LonDPP;
        int d = 0;
        for(int i=0; i < 7; i++){   // avoid using logarithm
            if(LonDPP*Math.pow((double)2,(double)i) <= largest_longDPP){d = i;}
        }
        return d+1;
    }

    // Compute the needed grids in one direction. In the form of int array{[ul_x,ul_y],[lr_x,lr_y]}
    private int[][] BoundaryGrids(int depth, double ul_lon, double lr_lon, double ul_lat, double lr_lat ){
        double step_amount = Math.pow((double)2,(double)depth);
        double x_step = (ROOT_LRLON - ROOT_ULLON)/step_amount;
        double y_step = (ROOT_ULLAT - ROOT_LRLAT)/step_amount;

        // Need to consider the case where the requested border line is right at the borderline of original images.
        int xNum_UL = Calculate_K(ul_lon, ROOT_LRLON, ROOT_ULLON, depth);
        int yNum_UL = Calculate_K(ul_lat, ROOT_LRLAT, ROOT_ULLAT, depth);

        int xNum_LR = Calculate_K(lr_lon, ROOT_LRLON, ROOT_ULLON, depth);
        int yNum_LR = Calculate_K(lr_lat, ROOT_LRLAT, ROOT_ULLAT, depth);

        int[][] result = {{xNum_UL,yNum_UL},{xNum_LR,yNum_LR}};
        return result;
    }

    // In the form of double array {{raster_ul_lon,raster_ul_lat},{raster_lr_lon,raster_lr_lat}}
    private double[][] GetRasterBoundary(int depth, int[][] BoundaryGrids){
        int ul_x = BoundaryGrids[0][0];
        int ul_y = BoundaryGrids[0][1];
        int lr_x = BoundaryGrids[1][0];
        int lr_y = BoundaryGrids[1][1];
        double step_amount = Math.pow((double)2,(double)depth);
        double x_step = (ROOT_LRLON - ROOT_ULLON)/step_amount;
        double y_step = (ROOT_ULLAT - ROOT_LRLAT)/step_amount;

        double raster_ul_lon = ROOT_ULLON + ul_x*x_step;
        double raster_ul_lat = ROOT_ULLAT - ul_y*y_step;
        double raster_lr_lon = ROOT_ULLON + (lr_x+1)*x_step;
        double raster_lr_lat = ROOT_ULLAT - (lr_y+1)*y_step;
        double[][] result = {{raster_ul_lon,raster_ul_lat},{raster_lr_lon,raster_lr_lat}};
        return result;
    }

    // Form the png to be chosen
    private String[][] GetImgFiles(int[][] BoundaryGrids, int depth){
        int ul_x = BoundaryGrids[0][0];
        int ul_y = BoundaryGrids[0][1];
        int lr_x = BoundaryGrids[1][0];
        int lr_y = BoundaryGrids[1][1];

        int NumXGrids = lr_x - ul_x + 1;
        int NumYGrids = lr_y - ul_y + 1;

        String[][] result = new String[NumYGrids][NumXGrids];
        for(int i=0;i<(NumYGrids);i++){
            for(int j=0;j<NumXGrids;j++){
                String part1 = "d"+Integer.toString(depth);
                String part2 = "x"+Integer.toString((j+ul_x));
                String part3 = "y"+Integer.toString((i+ul_y))+".png";
                result[i][j] = String.join("_",part1,part2,part3);
            }
        }
        return result;
    }

    int Calculate_K(double coordinate, double upperLimit, double lowerLimit, int depth){
        int i = 0;
        int flag = 0; // For computing particular k
        int step_amount = (int)Math.pow((double)2,(double)depth);
        double step = (upperLimit - lowerLimit)/step_amount;
        // If coordinate == lowerLimit, flag should be 0. If coordinate == upperLimit, i == step_amount, flag should also be 1.
        while(i <= step_amount){
            if(DoubleEqual(coordinate,(lowerLimit+i*step))&&(!DoubleEqual(coordinate-lowerLimit,0.0))) flag = 1;
            i += 1;
        }
        return (int) (Math.floor((coordinate-lowerLimit)/step)-flag);

    }

    boolean DoubleEqual(double a, double b){
        double delta = 0.000000000001;
        return Math.abs(a-b)<delta;
    }
}
