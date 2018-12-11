package schroder.stefan;

import java.util.Random;
import java.util.Stack;

import com.badlogic.gdx.math.Vector2;
import java.util.LinkedList;

public class Map{

    private Random random = new Random();
    public int gridx;
    public int gridy;
    public int xscale;
    public int yscale;
    public int scale;

    public int[][] valueGrid;
    public Vector2[][] directionGrid;

    public int[][] buildingGrid;

    //#region ASTAR
    private Vector2 end;
    private int[] goal;
    private int[] start;

    private LinkedList<int[]> openList;
    private LinkedList<int[]> closedList;

    private float [][] gscore;
    private float [][] fscore;
    private int [][][] cameFrom;

    private Stack<int[]> currentPath;
    //#endregion

    public Map(int gridx, int gridy, int xscale, int yscale){
        this.gridx = gridx;
        this.gridy = gridy;
        this.yscale = yscale;
        this.xscale = xscale;

        this.scale = (int)Math.sqrt(Math.pow(yscale,2) + Math.pow(xscale,2));

        constructBuildings();
        generate();
    }

    private void constructBuildings(){
        buildingGrid = new int[gridy][gridx];
        for(int y=0; y<gridy; y++){
            for(int x=0; x<gridx; x++){
                buildingGrid[y][x] = 0;
            }
        }

        int paddingX = 20;
        int paddingY = 10;
        int stndX = 15;
        int stndY = 10;
        int roadSize = 5;

        for(int y=paddingY; y<gridy-paddingY; y+=stndY+roadSize){
            for(int x=paddingX; x<gridx-paddingX; x+=stndX+roadSize){
                if(x+stndX+paddingX/2>gridx/2 && x < gridx/2 + paddingX/2)
                    if(y+stndY+paddingY/2>gridy/2 && y < gridy/2 + paddingY/2) continue;

                int extendRight = (random.nextInt(3)==2 && y+stndY+roadSize<gridy-paddingY) ? roadSize : 0;
                int extendUp = (random.nextInt(3)==2 && x+stndX+roadSize<gridx-paddingX) ? roadSize : 0;
                if(buildingGrid[y-1][x]==0 && buildingGrid[y][x-1]==0) if(random.nextBoolean()) continue;

                for(int suby=y; suby<y+stndY+extendRight; suby++){
                    for(int subx=x; subx<x+stndX+extendUp; subx++){
                        if(suby>=y+stndY && subx>=x+stndX) continue;

                        if(suby==y){
                            buildingGrid[suby][subx] = 2; //y needs to point up
                        }else if (suby==y+stndY+extendRight-1){
                            buildingGrid[suby][subx] = 3; //y needs to point down
                        }else if(subx==x){
                            buildingGrid[suby][subx] = 4; //x needs to point down
                        }else if(subx==x+stndX+extendUp-1){
                            buildingGrid[suby][subx] = 5; //x needs to point up
                        }else{
                            buildingGrid[suby][subx] = 1;
                        }
                    }
                }
            }
        }

    }

    public boolean isBlocked(Vector2 position){
        int yCord = (int)(position.y/yscale);
        int xCord = (int)(position.x/xscale);
        yCord %= gridy;
        xCord %= gridx;
        yCord = Math.abs(yCord);
        xCord = Math.abs(xCord);

        return (buildingGrid[yCord][xCord]>0);
    }

    public Vector2 unblockMe(Vector2 currentPosition, Vector2 velocity, float deltaTime){
        /*
        if(direction==null) direction = new Vector2(scale,0);
        else direction = direction.cpy().nor().scl(scale);

        while(isBlocked(currentPosition)){
            currentPosition.sub(direction);
        }

        return currentPosition;
        */
        //Vector2 nextPosition = currentPosition.cpy().mulAdd(velocity, deltaTime);
        if(/*isBlocked(nextPosition) || */isBlocked(currentPosition)){
            int yCord = (int)(currentPosition.y/yscale);
            int xCord = (int)(currentPosition.x/xscale);
            yCord %= gridy;
            xCord %= gridx;
            yCord = Math.abs(yCord);
            xCord = Math.abs(xCord);
            /*
            nextPosition = currentPosition.cpy().mulAdd(new Vector2(velocity.x, 0), deltaTime);
            if(isBlocked(nextPosition)){
                nextPosition = currentPosition.cpy().mulAdd(new Vector2(0,velocity.y), deltaTime);
                velocity.x*=-1;
            }else{
                velocity.y*=-1;
            }
            */

            if(buildingGrid[yCord][xCord]==2){
                velocity.y = -1*Math.abs(velocity.y);
                currentPosition.y -= yscale;
            }else if(buildingGrid[yCord][xCord]==3){
                velocity.y = Math.abs(velocity.y);
                currentPosition.y += yscale;
            }else if(buildingGrid[yCord][xCord]==4){
                velocity.x = -1*Math.abs(velocity.x);
                currentPosition.x -= xscale;
            }else if(buildingGrid[yCord][xCord]==5){
                velocity.x = Math.abs(velocity.x);
                currentPosition.x += xscale;
            }else{
                currentPosition.x = 0;
                currentPosition.y = 0;
            }

            /*
            velocity.x = 0;
            velocity.y = 0;
            */
        }

        return velocity;
    }


    public int[][] getValueGrid(){
        return valueGrid;
    }

    public Vector2[][] getDirectionMap(){
        return directionGrid;
    }

    public void generate(){
        valueGrid = new int[gridy][gridx];
        directionGrid = new Vector2[gridy][gridx];

        createValueGrid();
        integrateValueGrid();
    }

    private void createValueGrid(){
        /**
         * generates map perlin noise algorithm
         */
        //constants
        // int octaveCount = 5;
        int octaveCount = 6;

        //gen base
        float [][] base = new float[gridy][gridx];
        for(int y=0; y<gridy; y++){
            for(int x=0; x<gridx; x++){
                base[y][x] = random.nextFloat()*15;
            }
        }

		float[][][] smoothNoise = new float[octaveCount][][];
		float persistance = 0.7f;

		for (int i = 0; i < octaveCount; i++) {
            //smooth
            float[][] softNoise = new float[gridy][gridx];

            int samplePeriod = 1 << octaveCount*3/5; //quick pow 2
            // int samplePeriod = 1 << octaveCount*3/4; //quick pow 2
            float sampleFrequency = 1.0f / samplePeriod;

            for (int y = 0; y < gridy; y++) {

                int sampleY0 = (y / samplePeriod) * samplePeriod;
                int sampleY1 = (sampleY0 + samplePeriod) % gridy; //wrap edges

                float vertBlend = (y - sampleY0) * sampleFrequency;

                for (int x = 0; x < gridx; x++) {
                    int sampleX0 = (x / samplePeriod) * samplePeriod;
                    int sampleX1 = (sampleX0 + samplePeriod) % gridx; // wrap around
                    float horiBlend = (x - sampleX0) * sampleFrequency;

                    float top = interp(
                        base[sampleY0][sampleX0],
                        base[sampleY1][sampleX0],
                        vertBlend
                    );

                    float bottom = interp(
                        base[sampleY0][sampleX1],
                        base[sampleY1][sampleX1],
                        vertBlend
                    );

                    softNoise[y][x] = interp(
                        top,
                        bottom,
                        horiBlend
                    );
                }
            }

            smoothNoise[i] = softNoise;
		}

		float[][] perlinNoise = new float[gridy][gridx];

		float amplitude = 1.0f;
		float totalAmplitude = 0.0f;

		for (int octave = octaveCount - 1; octave >= 0; octave--) {
			amplitude *= persistance;
			totalAmplitude += amplitude;

			for (int y = 0; y < gridy; y++) {
				for (int x = 0; x < gridx; x++) {
					perlinNoise[y][x] += smoothNoise[octave][y][x] * amplitude;
				}
			}
		}

		for (int y = 0; y < gridy; y++) {
			for (int x = 0; x < gridx; x++) {
				valueGrid[y][x] = 3*(int)(perlinNoise[y][x]/totalAmplitude)+5;
			}
		}
    }


    public static float interp(float firstX, float nextX, float alpha) {
        return firstX * (1 - alpha) + alpha * nextX;
    }

    private void integrateValueGrid(){
        for(int y=0; y<gridy; y++){
            for(int x=0; x<gridx; x++){
                Vector2 newDir= new Vector2(0,0);
                if(y>0){
                    newDir.y -= valueGrid[y-1][x];
                    if(x>0) newDir.y -= 1.414*Float.valueOf(valueGrid[y-1][x-1]);
                    if(x<gridx-1) newDir.y -= 1.414*Float.valueOf(valueGrid[y-1][x+1]);

                }
                if(y<gridy-1){
                    newDir.y += valueGrid[y+1][x];
                    if(x>0) newDir.y += 1.414*Float.valueOf(valueGrid[y+1][x-1]);
                    if(x<gridx-1) newDir.y += 1.414*Float.valueOf(valueGrid[y+1][x+1]);
                }
                if(x>0){
                    newDir.x -= valueGrid[y][x-1];
                    if(y>0) newDir.x -= 1.414*Float.valueOf(valueGrid[y-1][x-1]);
                    if(y<gridy-1) newDir.x -= 1.414*Float.valueOf(valueGrid[y+1][x-1]);
                }
                if(x<gridx-1){
                    newDir.x += valueGrid[y][x+1];
                    if(y>0) newDir.x += 1.414*Float.valueOf(valueGrid[y-1][x+1]);
                    if(y<gridy-1) newDir.x += 1.414*Float.valueOf(valueGrid[y+1][x+1]);
                }
                //newDir.setLength(1);
                newDir.scl(-1);
                directionGrid[y][x]=newDir;

            }
        }
    }

    public void addHole(Vector2 position, float sizeFloat){
        int x = (int)position.x/xscale;
        int y = (int)position.y/yscale;
        int size = (int)sizeFloat/scale;

        int base = ((y>=gridy || y<0)||(x>=gridx || x<0)) ? 1 : valueGrid[y][x];
        for (int i = y-size; i < y+size; i++) {
            if(i>=gridy || i<0) continue;
            else{
                for (int j = x; (j-x)*(j-x) + (i-y)*(i-y) <= size*size; j--) {
                    if(j>=gridx || j<0) continue;
                    else{
                        int suggested = (int) (base - (size - Math.sqrt(Math.pow(x - j, 2) + Math.pow(y - i, 2)))) + random.nextInt(5);
                        valueGrid[i][j] = (valueGrid[i][j]<suggested) ? valueGrid[i][j] : suggested;
                        if(buildingGrid[i][j]==0) continue;
                        buildingGrid[i][j] = -1;
                    }
                }
                for (int j = x+1; (j-x)*(j-x) + (i-y)*(i-y) <= size*size; j++) {
                    if(j>=gridx || j<0) continue;
                    else{
                        int suggested = (int) (base - (size - Math.sqrt(Math.pow(x - j, 2) + Math.pow(y - i, 2)))) + random.nextInt(5);
                        valueGrid[i][j] = (valueGrid[i][j]<suggested) ? valueGrid[i][j] : suggested;
                        if(buildingGrid[i][j]==0) continue;
                        buildingGrid[i][j] = -1;
                    }

                }
            }
        }
        integrateValueGrid();
    }

    public Vector2 getCenterPosition(){
        return new Vector2((gridx*xscale)/2, (gridy*yscale)/2);
    }

    public float heuristicPath(Vector2 start, Vector2 end){
        return start.sub(end).len();
        // return end.sub(start).scl(1/scale).len();
    }

    public Stack<int[]> reconstruct(int[][][] cameFrom, int[] start, int[] end){
        int[] current = end.clone();
        while(current[0]!=start[0] || current[1]!=start[1]){
            // valueGrid[current[0]][current[1]] = 1000;
            current=cameFrom[current[0]][current[1]];
            currentPath.add(current);
        }
        return currentPath;

    }

    public void AStarInit(Vector2 startPoint){
        currentPath = new Stack<int[]>();
        end = getCenterPosition();
        goal = new int[]{(int)end.y/yscale, (int)end.x/xscale};
        start = new int[]{(int)startPoint.y/yscale, (int)startPoint.x/xscale};

        //open and closed list
        openList = new LinkedList<int[]>();
        closedList = new LinkedList<int[]>();

        gscore = new float[gridy][gridx];
        fscore = new float[gridy][gridx];
        cameFrom = new int[gridy][gridx][2];

        for(int y=0; y<gridy; y++){
            for(int x=0; x<gridx; x++){
                gscore[y][x] = Float.MAX_VALUE; //~max value
                fscore[y][x] = Float.MAX_VALUE; //~max value
                cameFrom[y][x] = new int[]{0,0};
            }
        }

        //reset current position g score
        gscore[start[0]][start[1]] = 0;

        //start fscore
        fscore[start[0]][start[1]] = heuristicPath(startPoint, end);

        openList.add(start);

    }

    public Stack<int[]> AStar(Vector2 startPoint){

        if(!openList.isEmpty()){
            //System.out.println(openList.size());

            //getting the point with the lowest F scrore
            int[] current = openList.peek();
            float lowestF = fscore[current[0]][current[1]];
            for(int[] aPlace :  openList){
                if(fscore[aPlace[0]][aPlace[1]]<lowestF){
                    lowestF = fscore[aPlace[0]][aPlace[1]];
                    current = aPlace;
                }
            }

            // System.out.println("y:"+current[0]+" x:"+current[1]);
            if(current[0]==goal[0] && current[1]==goal[1]){
                return reconstruct(cameFrom, start, goal);
            }

            if(!openList.remove(current)) System.out.println("opps");
            closedList.add(current);

            //getting neighbours
            int starty = (current[0]-1<=0) ? 0 : current[0]-1;
            int endy = (current[0]+1>=gridy) ? gridy-1 : current[0]+1;

            int startx = (current[1]-1<=0) ? 0 : current[1]-1;
            int endx = (current[1]+1>=gridx) ? gridx-1 : current[1]+1;

            for(int y=starty; y<=endy; y++){
                nextX:
                for(int x=startx; x<=endx; x++){
                    //System.out.println(x+" "+y);
                    if(x==current[1] && y==current[0]
                        || buildingGrid[y][x]>0
                        || buildingGrid[(y+2)%gridy][x]>0
                        || buildingGrid[Math.abs(y-2)%gridy][x]>0
                        || buildingGrid[y][Math.abs(x-2)%gridx]>0
                        || buildingGrid[y][(x+2)%gridx]>0) continue;

                    int[] thisPoint = {y,x};

                    for(int[] aPlace :  closedList){
                        if(aPlace[0]==y && aPlace[1]==x) continue nextX;
                    }

                    Vector2 currentDirection = new Vector2(x-current[1], y-current[0]).nor();
                    Vector2 landDirection = directionGrid[current[0]][current[1]].cpy().setLength(0.4f);

                    float scaler = landDirection.cpy().dot(currentDirection)/ ( currentDirection.len()*currentDirection.len());
                    float withDirection = currentDirection.add(landDirection.scl(scaler)).len();

                    float possableG = gscore[current[0]][current[1]] + withDirection*4 + (float)Math.sqrt(Math.pow(current[1]-x,2)+Math.pow(current[0]-y,2));//find better way to do this

                    boolean alreadyIn = false;
                    for(int[] aPlace : openList){
                        if(aPlace[0]==y && aPlace[1]==x){
                            alreadyIn = true;
                            if(gscore[aPlace[0]][aPlace[1]]<=possableG) continue nextX;
                        }
                    }
                    if(!alreadyIn) openList.add(thisPoint);


                    cameFrom[thisPoint[0]][thisPoint[1]] = new int[]{current[0],current[1]};

                    gscore[thisPoint[0]][thisPoint[1]] = possableG;
                    fscore[thisPoint[0]][thisPoint[1]] = possableG + heuristicPath(new Vector2(x*xscale, y*yscale), end);
                }
            }

            // valueGrid[current[0]][current[1]] = -100;
            //openList.pop();

        }
        return null;

    }
}