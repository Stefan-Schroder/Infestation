package schroder.stefan.objects;

import java.util.Stack;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import schroder.stefan.Map;

public class Vehicle{
    private Vector2 position;
    private Vector2 velocity;

    private Texture carText;
    private Sprite car;

    private boolean ready;
    private boolean complete;

    private Map map;

    private Stack<int[]> path;

    //test
    private int counter = 100000;
    private int[] nextPos;

    public Vehicle(Vector2 position, Map map, boolean ready){
        //this.position = position;
        this.position = new Vector2();
        this.position.x = position.x;
        this.position.y = position.y;
        this.map = map;
        this.velocity = new Vector2(0,0);
		this.carText = new Texture("sprites/vehicle/4by4.png");
        this.car = new Sprite(carText);
        car.setOrigin(50, 50);
        this.ready = ready;
        map.AStarInit(position);
        complete = false;
    }

    public void draw(Batch currentBatch){
        car.setPosition(position.x-50, position.y-50);
        car.setRotation(velocity.angle());
        car.setScale(0.5f, 0.5f);
        car.draw(currentBatch);
    }

    public void update(float dt){
        if(!ready){
            path = map.AStar(position);
            if(path!=null){
                ready = true;
            }
        }else{
            if(path.isEmpty()){
                complete = true;
                ready=false;
            }else{
                if(counter>=1000){
                    nextPos = path.pop();
                    counter = 0;
                }
                Vector2 oldPos = position.cpy();
                position.lerp(new Vector2(nextPos[1]*map.xscale,nextPos[0]*map.yscale),0.001f);
                counter++;
                velocity.lerp(position.cpy().sub(oldPos),0.001f);
                //System.out.println(counter);

            }

        }
        /*
        velocity = new Vector2(map.getCenterPosition());
        velocity.sub(position);
        velocity.scl(0.3f);

        position.mulAdd(velocity, dt);
        */
    }

    public Vector2 getPosition(){
        return position;
    }

    public boolean isReady(){
        return ready;
    }

    public boolean isComplete(){
        return complete;
    }

}