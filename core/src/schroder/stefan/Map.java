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
        float [][] noiseGrid = new float[gridy][gridx]; 
        for(int y=0; y<gridy; y++){
            for(int x=0; x<gridx; x++){
                //valueGrid[y][x] = (int)(400/(Math.abs(gridx/2-x)+Math.abs(gridy/2-y)+0.001))+random.nextInt(2);
                //valueGrid[y][x] = (int)random.nextInt(5);

                //perlin noiseish
                //valueGrid[y][x] = noise(x,y);
                noiseGrid[y][x] = noise(x,y);
            }
        }
        noiseGrid = generatePerlinNoise(noiseGrid, 5);

        for(int y=0; y<gridy; y++){
            for(int x=0; x<gridx; x++){
                //valueGrid[y][x] = (int)(400/(Math.abs(gridx/2-x)+Math.abs(gridy/2-y)+0.001))+random.nextInt(2);
                //valueGrid[y][x] = (int)random.nextInt(5);

                //perlin noiseish
                //valueGrid[y][x] = noise(x,y);
                valueGrid[y][x] = 5*(int)noiseGrid[y][x];
            }
        }
    }

    private float noise(int x, int y){
        float noise = 0.0f;
        Vector2 position = new Vector2(x,y);
        noise = 10*(float) Math.sin(position.dot(12.9898f, 78.233f) * 43758.5453123f);
        return random.nextFloat()*10;
    }

    public static float interpolate (float x0, float x1, float alpha) {
        /**
         * Not my code, pasted here to test
         */
        return x0 * (1 - alpha) + alpha * x1;
    }

    public static float[][] generateSmoothNoise (float[][] baseNoise, int octave) {
        /**
         * Not my code, pasted here to test
         */
		int width = baseNoise.length;
		int height = baseNoise[0].length;
		float[][] smoothNoise = new float[width][height];

		int samplePeriod = 1 << octave; // calculates 2 ^ k
		float sampleFrequency = 1.0f / samplePeriod;
		for (int i = 0; i < width; i++) {
			int sample_i0 = (i / samplePeriod) * samplePeriod;
			int sample_i1 = (sample_i0 + samplePeriod) % width; // wrap around
			float horizontal_blend = (i - sample_i0) * sampleFrequency;

			for (int j = 0; j < height; j++) {
				int sample_j0 = (j / samplePeriod) * samplePeriod;
				int sample_j1 = (sample_j0 + samplePeriod) % height; // wrap around
				float vertical_blend = (j - sample_j0) * sampleFrequency;
				float top = interpolate(baseNoise[sample_i0][sample_j0], baseNoise[sample_i1][sample_j0], horizontal_blend);
				float bottom = interpolate(baseNoise[sample_i0][sample_j1], baseNoise[sample_i1][sample_j1], horizontal_blend);
				smoothNoise[i][j] = interpolate(top, bottom, vertical_blend);
			}
		}

		return smoothNoise;
    }

    public static float[][] generatePerlinNoise (float[][] baseNoise, int octaveCount) {
        /**
         * Not my code, pasted here to test
         */
		int width = baseNoise.length;
		int height = baseNoise[0].length;
		float[][][] smoothNoise = new float[octaveCount][][]; // an array of 2D arrays containing
		float persistance = 0.7f;

		for (int i = 0; i < octaveCount; i++) {
			smoothNoise[i] = generateSmoothNoise(baseNoise, i);
		}

		float[][] perlinNoise = new float[width][height]; // an array of floats initialised to 0

		float amplitude = 1.0f;
		float totalAmplitude = 0.0f;

		for (int octave = octaveCount - 1; octave >= 0; octave--) {
			amplitude *= persistance;
			totalAmplitude += amplitude;

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					perlinNoise[i][j] += smoothNoise[octave][i][j] * amplitude;
				}
			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				perlinNoise[i][j] /= totalAmplitude;
			}
		}

		return perlinNoise;
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