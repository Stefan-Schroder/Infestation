package schroder.stefan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class WeaponDamage{
    public Vector2 position;
    public long time;
    public float size;
    public float force;
    public float killZone;
    public int gun;
    public boolean completed;

    //animation
    private static final int FRAMECOL = 14, FRAMEROW = 1;
    private float centerY, centerX;
    Animation<TextureRegion> explosionAni;
    Texture explosionSheet;

    float stateTime;

    //sound
    private Sound explosionSound;
    private boolean soundStarted;


    public WeaponDamage(Vector2 position, long time, float size, float force, float killZone, int gun){
        /**
         * GUN: 1=Turret 1=Bomb 2=Cluster
         */
        this.position = position;
        this.time = time;
        this.size = size;
        this.force = force;
        this.killZone = killZone;
        this.gun = gun;
        completed = false;
        soundStarted = false;



        //animation
        stateTime = 0;
        switch(gun){
            case 0:
                explosionSheet = new Texture(Gdx.files.internal("sprites/bombs/1.png"));
                explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosions/Turret2.wav"));
                break;
            case 1:
                explosionSheet = new Texture(Gdx.files.internal("sprites/bombs/1.png"));
                explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosions/Bomb.wav"));
                break;
            case 2:
                explosionSheet = new Texture(Gdx.files.internal("sprites/bombs/1.png"));
                explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosions/Cluster.wav"));
                break;
        }

        TextureRegion[][] tempSheet = TextureRegion.split(explosionSheet, explosionSheet.getWidth()/FRAMECOL, explosionSheet.getHeight()/FRAMEROW);

        centerY = (explosionSheet.getHeight()/FRAMEROW)/2;
        centerX = (explosionSheet.getWidth()/FRAMECOL)/2;


        TextureRegion[] expFrames = new TextureRegion[FRAMECOL*FRAMEROW];
        int index = 0;
        for(int i=0; i<FRAMEROW; i++){
            for(int j=0; j<FRAMECOL; j++){
                expFrames[index++] = tempSheet[i][j];
            }
        }

        explosionAni = new Animation<TextureRegion>(0.02f, expFrames);

        //sound
    }

    public void playSound(){
        if(soundStarted) return;
        explosionSound.play(0.1f);
        soundStarted = true;
    }

    public boolean isTime(long currentTime){
        long diff = time-currentTime;
        //System.out.println(diff);
        switch(gun){
            case 0:
                playSound();
                break;
            case 1:
                if(diff<350*1000000){
                    playSound();
                }
                break;
            case 2:
                if(diff<35*1000000){
                    playSound();
                }
                break;
        }
        return time<=currentTime;
    }

    public boolean isCompleted(){
        completed = explosionAni.isAnimationFinished(stateTime);
        return completed;
    }

    public void draw(Batch batch, float deltaTime){
        if(completed) return;
        float scale = 0.1f;
        switch(gun){
            case 0:
                scale = 0.1f;
                break;
            case 1:
                scale = 0.8f;
                break;
            case 2:
                scale = 0.4f;
                break;
        }
        stateTime += deltaTime;
        TextureRegion currentFrame = explosionAni.getKeyFrame(stateTime, true);

        batch.draw(currentFrame, position.x-centerX*scale, position.y-centerY*scale, scale*centerX*2, scale*centerY*2);
    }

    public void dispose(){
        explosionSheet.dispose();
        //explosionSound.dispose();
    }
}