package schroder.stefan.objects;

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

    private Map map;

    public Vehicle(Vector2 position, Map map){
        System.out.println("car spawned");
        this.position = position;
        this.map = map;
        this.velocity = new Vector2(0,0);
		this.carText = new Texture("sprites/vehicle/4by4.png");
        this.car = new Sprite(carText);
        car.setOriginCenter();
    }

    public void draw(Batch currentBatch){
        car.setPosition(position.x, position.y);
        car.setRotation(velocity.angle());
        car.setScale(0.5f, 0.5f);
        car.draw(currentBatch);
    }

    public void update(float dt){
        velocity = new Vector2(map.getCenterPosition());
        velocity.sub(position);
        velocity.scl(0.3f);

        position.mulAdd(velocity, dt);

    }

    public Vector2 getPosition(){
        return position;
    }

}