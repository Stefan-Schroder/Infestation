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
    private Vector2 direction;

    private Texture carText;
    private Sprite car;

    private boolean ready;
    private boolean complete;

    private Map map;

    private Stack<int[]> path;

    private int[] nextPos;

    public Vehicle(Vector2 position, Map map, boolean ready){
        //this.position = position;
        this.position = new Vector2();
        this.position.x = position.x;
        this.position.y = position.y;

        this.map = map;

        this.velocity = new Vector2(0,0);
        this.direction = new Vector2(0,0);

		this.carText = new Texture("sprites/vehicle/4by4.png");
        this.car = new Sprite(carText);
        car.setOrigin(50, 50);

        this.ready = ready;
        this.complete = false;

        map.AStarInit(position);
    }

    public void draw(Batch currentBatch){
        direction.lerp(velocity, 0.2f);
        car.setPosition(position.x-50, position.y-50);
        car.setRotation(direction.angle());
        car.setScale(0.4f, 0.4f);
        car.draw(currentBatch);
    }

    public void update(float dt){
        if(!ready && !complete){
            path = map.AStar(position);
            if(path!=null && !complete){
                ready = true;
            }
        }else{
            if(path.isEmpty()){
                complete = true;
                ready=false;
            }else{

                if(nextPos==null){
                    nextPos = path.pop();
                }
                Vector2 nextDes = new Vector2(nextPos[1]*map.xscale,nextPos[0]*map.yscale);
                while(position.dst(nextDes)<15 && !path.isEmpty()){
                    nextDes = new Vector2(nextPos[1]*map.xscale,nextPos[0]*map.yscale);
                    nextPos = path.pop();
                }

                velocity = nextDes;
                velocity.sub(position.cpy());
                velocity.setLength(80);

                Vector2 landDirection = map.directionGrid[nextPos[0]][nextPos[1]].cpy();
                float scaler = landDirection.cpy().dot(velocity)/ ( velocity.len()*velocity.len());

                Vector2 addedVelocity = velocity.cpy().scl(scaler);
                addedVelocity.limit(30);
                velocity.add(addedVelocity);

                // velocity.clamp(40,100);

                position.mulAdd(velocity, dt);

            }

        }
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