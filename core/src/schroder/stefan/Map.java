package schroder.stefan;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;

public class Map{

    private static final float PI = 3.141592654f;

    private Random random = new Random();
    public int gridx;
    public int gridy;
    public int xscale;
    public int yscale;
    public int scale;

    public int[][] valueGrid;
    public Vector2[][] directionGrid;

    public Map(int gridx, int gridy, int xscale, int yscale){
        this.gridx = gridx;
        this.gridy = gridy;
        this.yscale = yscale;
        this.xscale = xscale;

        this.scale = (int)Math.sqrt(Math.pow(yscale,2) + Math.pow(xscale,2));

        generate();
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
        int octaveCount = 5;

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
				valueGrid[y][x] = 5*(int)(perlinNoise[y][x]/totalAmplitude);
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
                    }
                }
                for (int j = x+1; (j-x)*(j-x) + (i-y)*(i-y) <= size*size; j++) {
                    if(j>=gridx || j<0) continue;
                    else{
                        int suggested = (int) (base - (size - Math.sqrt(Math.pow(x - j, 2) + Math.pow(y - i, 2)))) + random.nextInt(5);
                        valueGrid[i][j] = (valueGrid[i][j]<suggested) ? valueGrid[i][j] : suggested;
                    }

                }
            }
        }
        integrateValueGrid();
    }
}