package schroder.stefan.particle;

import com.badlogic.gdx.math.Vector2;

import java.util.LinkedList;

//Other classes
import schroder.stefan.Map;

public class ZombieParticle{
    private final int height;
    private final int width;
    private Vector2 position;
    private Vector2 velocity;
    private Vector2 acceleration;

    private Map map;

    private float life;
    private float maxSpeed;
    private float maxForce;

    public ZombieParticle(Vector2 position, int width, int height, Map map){
        this.width = width;
        this.height = height;
        this.map = map;
        reset(position);
    }

    public void reset(Vector2 position){
        this.position = position;
        this.velocity = new Vector2(0,0);
        this.acceleration = new Vector2(0,0);
        this.life = 20;
        this.maxSpeed = 100;
        this.maxForce = 200;
    }

    public Vector2 getPosition(){
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void update(float deltaTime){
        if(position.x<0) {
            position.x = width-10;
        }else if(position.x>width-10){
            position.x = 0;
        }if(position.y<0){
            position.y = height-10;
        }else if(position.y>height-10) {
            position.y = 0;
        }if (position.x>=width/2-5 && position.x<=width/2+5 && position.y>=height/2-5 && position.y<=height/2+5){
            life = -1;
            return;
        }

        velocity.mulAdd(acceleration, deltaTime);
        velocity.limit(maxSpeed);

        position.mulAdd(velocity, deltaTime);

        acceleration.scl(0);
        life -= deltaTime;

    }

    public void applyForce(Vector2 force){
        acceleration.add(force);
    }

    public void seek(Vector2 destination, float weight){
        Vector2 desired = new Vector2(destination.x-position.x,destination.y-position.y);
        desired.setLength(maxSpeed);

        Vector2 steer = new Vector2(desired.x-velocity.x, desired.y-velocity.y);
        steer.scl(weight * (50/life));
        steer.limit(maxForce);

        applyForce(steer);
    }

    public void follow(float weight){
        Vector2[][] vmap = map.directionGrid;
        int xposition = (int) (position.x/(width/map.gridx));
        int yposition = (int) (position.y/(height/map.gridy));
        if(xposition>=map.gridx) xposition = map.gridx-1;
        if(xposition<0) xposition = 0;
        if(yposition>=map.gridy) yposition = map.gridy-1;
        if(yposition<0) yposition = 0;
        
        Vector2 desired = vmap[yposition][xposition];
        desired.setLength(maxSpeed);

        Vector2 steer = new Vector2(desired.x-velocity.x, desired.y-velocity.y);
        steer.scl(weight);
        steer.limit(maxForce);

        applyForce(steer);
    }

    public void separate(LinkedList<ZombieParticle> allParticles, float weight){
        float desiredSpace = 10;
        Vector2 sum = new Vector2(0,0);
        int countAverage = 0;
        for(ZombieParticle eachPart : allParticles){
            float dist = Vector2.dst(position.x, position.y, eachPart.getPosition().x, eachPart.getPosition().y);
            if(dist > 0 && dist < desiredSpace){
                Vector2 difference = new Vector2(position.x - eachPart.getPosition().x, position.y - eachPart.getPosition().y);
                difference.nor();
                difference.scl(1/dist);
                sum.add(difference);
                countAverage++;
            }
        }

        if(countAverage>0){
            sum.setLength(maxSpeed);

            Vector2 steer = new Vector2(sum.x-velocity.x, sum.y-velocity.y);
            steer.scl(weight);
            steer.limit(maxForce);

            applyForce(steer);
        }
    }

    public void align(LinkedList<ZombieParticle> allParticles, float weight){
        float neighbourhood = 10;
        Vector2 sum = new Vector2(0,0);
        int countAverage = 0;
        for(ZombieParticle eachPart : allParticles){
            float dist = Vector2.dst(position.x, position.y, eachPart.getPosition().x, eachPart.getPosition().y);
            if(dist > 0 && dist < neighbourhood){
                sum.add(eachPart.getVelocity());
                countAverage++;
            }
        }

        if(countAverage>0){
            sum.scl(1/((float)countAverage));
            sum.setLength(maxSpeed);

            Vector2 steer = new Vector2(sum.x-velocity.x, sum.y-velocity.y);
            steer.scl(weight);
            steer.limit(maxForce);

            applyForce(steer);
        }

    }

    public void cohesion(LinkedList<ZombieParticle> allParticles, float weight){
        float neighbourhood = 10;
        Vector2 sum = new Vector2(0,0);
        int countAverage = 0;
        for(ZombieParticle eachPart : allParticles){
            float dist = Vector2.dst(position.x, position.y, eachPart.getPosition().x, eachPart.getPosition().y);
            if(dist > 0 && dist < neighbourhood){
                sum.add(eachPart.getPosition());
                countAverage++;
            }
        }

        if(countAverage>0){
            sum.scl(1/((float)countAverage));
            seek(sum, weight);
        }
    }

    public void flock(LinkedList<ZombieParticle> allParticles, float separateWeight, float cohesionWeight, float alignWeight){
        //broken
        float neighbourhood = 20;
        float desiredSpace = 5;
        Vector2 sepSum = new Vector2(0,0);
        Vector2 cohSum = new Vector2(0,0);
        Vector2 aliSum = new Vector2(0,0);
        int countAverage = 0;
        for(ZombieParticle eachPart : allParticles){
            float dist = Vector2.dst(position.x, position.y, eachPart.getPosition().x, eachPart.getPosition().y);
            if (dist > 0) {
                if(dist < neighbourhood){
                    cohSum.add(eachPart.getVelocity());
                    aliSum.add(eachPart.getPosition());
                    countAverage++;
                }
                if(dist < desiredSpace){
                    Vector2 difference = new Vector2(position.x - eachPart.getPosition().x, position.y - eachPart.getPosition().y);
                    difference.nor();
                    difference.scl(1/dist);
                    sepSum.add(difference);
                    countAverage++;
                }
            }
        }

        if(countAverage>0){
            aliSum.scl(1/((float)countAverage));
            seek(aliSum, alignWeight);

            cohSum.scl(1/((float)countAverage));
            //cohSum.scl(cohesionWeight/(separateWeight+cohesionWeight));
            //cohSum.mulAdd(sepSum, separateWeight/(separateWeight+cohesionWeight));
            cohSum.add(sepSum);
            cohSum.setLength(maxSpeed*2);

            Vector2 cohSteer = new Vector2(cohSum.x-velocity.x, cohSum.y-velocity.y);
            cohSteer.scl(cohesionWeight+separateWeight);
            cohSteer.limit(maxForce*2);

            applyForce(cohSteer);
        }
    }

    public boolean isDead(){
        return life<=0;
    }

	public void kill() {
        life = -1;
	}

}