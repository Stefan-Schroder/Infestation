package schroder.stefan;

import com.badlogic.gdx.math.Vector2;

public class WeaponDamage{
    public Vector2 position;
    public long time;
    public float size;
    public float force;
    public float killZone;
    public int gun;

    public WeaponDamage(Vector2 position, long time, float size, float force, float killZone, int gun){
        /**
         * GUN: 0=Turret 1=Bomb 2=Cluster
         */
        this.position = position;
        this.time = time;
        this.size = size;
        this.force = force;
        this.killZone = killZone;
        this.gun = gun;
    }

    public boolean isTime(long currentTime){
        return time<=currentTime;
    }
}