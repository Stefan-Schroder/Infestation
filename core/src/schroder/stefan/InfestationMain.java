package schroder.stefan;

import java.util.LinkedList;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.TimeUtils;

//other classes
import schroder.stefan.particle.ParticleController;
import schroder.stefan.particle.ZombieParticle;;

public class InfestationMain extends ApplicationAdapter {
	private ShapeRenderer shapeRenderer;
	private BitmapFont font;
	private SpriteBatch batch;
	private SpriteBatch shadedBatch;

	private OrthographicCamera camera;

	private ParticleController zombiePool;

	private Random random;

	private int[][] valueGrid;

	private int width;
	private int height;

	private FrameBuffer frameBuffer;

	private String vertexShader;  
	private String fragmentShader;
	private ShaderProgram shader;

	private Texture zombieText;
	private Sprite zombie;

	private Texture bloodText;
	private Sprite blood;

	private Texture houseText;
	private Sprite house;

	private Sprite screen;

	private int xscale;
	private int yscale;

	private int gridX;
	private int gridY;

	private int gunMode;
	private long lastCarpetDrop;
	private long lastBombDrop;

	private Map map;

	private long lastZombieTime;

	@Override
	public void create () {
		//Window size setup
		width = Gdx.graphics.getBackBufferWidth();
		height = Gdx.graphics.getBackBufferHeight();
		gridX = 200;
		gridY = 100;
		xscale = width / gridX;
		yscale = height / gridY;


		//Map objects
		map = new Map(gridX, gridY, xscale, yscale);


		//Camera settup
		camera = new OrthographicCamera();
		camera.setToOrtho(false, height, width);


		//used classes
		random = new Random();


		//renderers
		shapeRenderer = new ShapeRenderer();
		batch = new SpriteBatch();
		font = new BitmapFont();


		//Particle Thread Initialize
		zombiePool = new ParticleController(width, height, map);
		zombiePool.start();


		//shader initialize
		frameBuffer = new FrameBuffer(Format.RGBA8888, width, height, true);

		fragmentShader = Gdx.files.internal("shaders/fragment/using/droneCamera.glsl").readString();
		vertexShader = Gdx.files.internal("shaders/vertex/using/vertex.glsl").readString();
		shader = new ShaderProgram(vertexShader, fragmentShader);

		if(!shader.isCompiled()){
			throw new GdxRuntimeException("Couldn't compile shader: " + shader.getLog());
		}else{
			System.out.println(shader.getLog());
		}

		shadedBatch = new SpriteBatch(10, shader);
		//end shader


		//sprite initialize 
		zombieText = new Texture("Sprites/zombie2.png");
		zombie = new Sprite(zombieText);

		bloodText = new Texture("Sprites/blood.png");
		blood = new Sprite(bloodText);
		
		houseText = new Texture("Sprites/house.png");
		house = new Sprite(houseText);
		house.setPosition((width - houseText.getWidth())/2, (height - houseText.getHeight())/2);
		//end sprite


		//Initialize Constants
		//gun
		gunMode = 0;
		lastCarpetDrop = 0;
		lastBombDrop = 0;

		//other
		lastZombieTime = 0;

	}

	@Override
	public void render () {
		//render buffer
		frameBuffer.begin();

		Gdx.gl.glClearColor(1.1f, 0.1f, 0.1f, 1);

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		//render map
		valueGrid = map.valueGrid.clone();
		for(int y=0; y<gridY; y++){
			for(int x=0; x<gridX; x++){
				shapeRenderer.setColor(
					Float.valueOf(valueGrid[y][x])/200,
					Float.valueOf(valueGrid[y][x])/200, 
					Float.valueOf(valueGrid[y][x])/200,
					1
				);
				shapeRenderer.rect(x*xscale,y*yscale,xscale,yscale);
			}
		}
		shapeRenderer.end();
		//end map render

		//start rendering zombies
		LinkedList<ZombieParticle> livingParts = zombiePool.getLiveParticle();
		LinkedList<ZombieParticle> deadParts = zombiePool.getDeadParticle();

		batch.begin();

		//rendering blood of the dead
		for(ZombieParticle oneParticle : deadParts) {
			if(oneParticle!=null){
				batch.draw(blood, oneParticle.getPosition().x, oneParticle.getPosition().y,10,10,20,20,0.7f,0.7f,oneParticle.getVelocity().angle() );
			}
		}

		//rendering living dead
		for(ZombieParticle oneParticle : livingParts) {
		    //shapeRenderer.setColor(color);
			if(oneParticle!=null){
				if(random.nextInt(20)==5) zombie.flip(false, true);
				batch.draw(zombie, oneParticle.getPosition().x, oneParticle.getPosition().y,10,10,20,20,0.7f,0.7f,oneParticle.getVelocity().angle() );
			}
		}

		//rendering buildings
		house.draw(batch);

		batch.end();

		frameBuffer.end();
		//end frame buffer

		
		//clear view
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);


		//making frameBuffer
		Texture screenTexture = frameBuffer.getColorBufferTexture();
		screen = new Sprite(screenTexture);
		screen.setPosition(0, 0);
		screen.flip(false, true);


		//Render section with shader
		shadedBatch.begin();

		//set variables
		int a = shader.getUniformLocation("u_time");
		float currentTime = (float)((float) TimeUtils.nanoTime() * Math.pow(10, -9));
		shader.setUniformf(a, currentTime);

		//draw shaded objects
		screen.draw(shadedBatch);

		shadedBatch.end();
		//end shaded batch


		//render UI
		batch.begin();

		font.draw(batch, "mouse position: ("+Gdx.input.getX()+", "+(height-Gdx.input.getY())+")",5,15);
		font.draw(batch, zombiePool.getSizes(), 5, 30);
		font.draw(batch, "Gunmode: "+gunMode, 5, 45);

		batch.end();
		//end UI

		
		buttonCheck();
		

		//spawn zombies
		if(TimeUtils.millis()-lastZombieTime>10000+random.nextInt(10000)){
			Vector2 position;
			switch(random.nextInt(3)){
				case 0:
					position = new Vector2(random.nextInt(width), 10);
					break;
				case 1:
					position = new Vector2(random.nextInt(width), height-10);
					break;
				case 2:
					position = new Vector2(10, random.nextInt(height));
					break;
				default:
					position = new Vector2(width-10, random.nextInt(height));
			}
			for(int i=0; i<100; i+= 1) {
				Vector2 displacement = new Vector2(i + random.nextFloat()*100, random.nextFloat()*100 - i);
				displacement.add(position);
				zombiePool.createParticle(displacement);
			}
			lastZombieTime = TimeUtils.millis();
		}
	}

	private void buttonCheck(){
		//Button check
		if(Gdx.input.isKeyJustPressed(Input.Keys.TAB)){
			gunMode = (gunMode+1)%3;
		}

		if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){

			//new Damage
			Vector2 damagePosition = new Vector2(Gdx.input.getX() + random.nextFloat()*2 -1, height - Gdx.input.getY() + random.nextFloat()*2 - 1);
			long dropTime = TimeUtils.nanoTime();
			float damageSize = 0;
			float damageForce = 0;
			float killZone = 0;

			boolean addDamage = true;

			switch(gunMode){
				case 1:
					//bomb
					if(TimeUtils.nanoTime() - lastBombDrop<10*Math.pow(10,9)){
						addDamage = false;
						break;
					}
					dropTime += Math.pow(10, 9);
					damageSize = 200;
					damageForce = 1000;
					killZone = 100;
					lastBombDrop = TimeUtils.nanoTime();
					break;
				case 2:
					//carpet
					if(TimeUtils.nanoTime() - lastCarpetDrop<50*Math.pow(10,6)){
						addDamage = false;
						break;
					}
					dropTime += Math.pow(10, 9);
					damageSize = 60;
					damageForce = 1000;
					killZone = 40;
					lastCarpetDrop = TimeUtils.nanoTime();
					break;
				default:
					//gun
					dropTime = 0;
					damageSize = 10;
					damageForce = 200;
					killZone = 10;
			}

			if(addDamage) zombiePool.damage.add(new WeaponDamage(damagePosition, dropTime, damageSize, damageForce, killZone, gunMode));
		}
	}

	@Override
	public void dispose () {
		shadedBatch.dispose();
		shapeRenderer.dispose();
		batch.dispose();
	}
}