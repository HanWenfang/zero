/******************************************************************************
 * Copyright 2016 Fabian Lupa                                                 *
 ******************************************************************************/

package com.flaiker.zero.helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Manages animations of entities
 */
public class AnimationManager {
    public static final String LOG = AnimationManager.class.getSimpleName();

    private HashMap<String, Animation> animationList;
    private HashMap<String, Animation> idleAnimationList;
    private Sprite                     sprite;
    private Animation                  currentAnimation;
    private TextureRegion              unanimatedRegion;
    private float                      elapsedTime;
    private AnimationDirection         animationDirection;

    private float maximumAddedIdleTime = 0;
    private float minimumIdleTime      = 6;
    private float currentIdleTime      = 0;
    private float currentAddedIdleTime = 0;
    Random generator = new Random();

    public AnimationManager(Sprite sprite) {
        animationList = new HashMap<>();
        idleAnimationList = new HashMap<>();
        this.sprite = sprite;
        this.currentAnimation = null;
        this.unanimatedRegion = new TextureRegion(sprite.getTexture(), sprite.getU(), sprite.getV(), sprite.getU2(),
                                                  sprite.getV2());
        this.animationDirection = AnimationDirection.RIGHT;
        this.elapsedTime = 0;
    }

    public void registerAnimation(String key, Animation animation) {
        animationList.put(key, animation);
    }

    public void registerAnimation(String entityName, String key, TextureAtlas atlas, float frameDuration) {
        animationList.put(key, getAnimationFromKey(entityName, key, atlas, frameDuration));
    }

    public void registerIdleAnimation(String entityName, String key, TextureAtlas atlas, float frameDuration) {
        idleAnimationList.put(key, getAnimationFromKey(entityName, key, atlas, frameDuration));
    }

    private Animation getAnimationFromKey(String entityName, String key, TextureAtlas atlas, float frameDuration) {
        SortedMap<Integer, TextureRegion> list = new TreeMap<>();
        for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
            String[] name = region.name.split("_");
            if (name.length == 2 && name[0].equals(entityName) && name[1].startsWith(key)) {
                String animationName[] = name[1].split("-");
                if (animationName.length == 2) {
                    try {
                        int frame = Integer.parseInt(animationName[1]);
                        list.put(frame, region);
                    } catch (NumberFormatException e) {
                        Gdx.app.log(LOG, "Frame could not be parsed");
                    }
                }
            }
        }
        return new Animation(frameDuration, list.values().toArray(new TextureRegion[list.size()]));
    }

    public void updateAnimationFrameDuration(String key, float newDuration) {
        Animation foundAnimation = animationList.get(key);
        if (foundAnimation != null) {
            foundAnimation.setFrameDuration(newDuration);
        }
    }

    public void runAnimation(String key) {
        runAnimation(key, AnimationDirection.RIGHT);
    }

    public void runAnimation(String key, AnimationDirection direction) {
        Animation foundAnimation = animationList.get(key);

        if (foundAnimation != null) {
            if (currentAnimation != foundAnimation) elapsedTime = 0;
            currentAnimation = foundAnimation;
            animationDirection = direction;
        }
    }

    public void runAnimation(Animation animation) {
        runAnimation(animation, AnimationDirection.RIGHT);
    }


    public void runAnimation(Animation animation, AnimationDirection direction) {
        elapsedTime = 0;
        currentAddedIdleTime = 0;
        currentAnimation = animation;
        animationDirection = direction;
    }

    public void updateSprite() {
        if (currentAnimation != null) {
            currentIdleTime = 0;
            currentAddedIdleTime = 0;
            elapsedTime += Gdx.graphics.getDeltaTime();
            sprite.setRegion(currentAnimation.getKeyFrame(elapsedTime, true));
            if (idleAnimationList.containsValue(currentAnimation) &&
                currentAnimation.isAnimationFinished(elapsedTime)) {
                stopAnimation();
            }
        } else {
            currentIdleTime += Gdx.graphics.getDeltaTime();
            sprite.setRegion(unanimatedRegion);
            if (currentIdleTime > minimumIdleTime && idleAnimationList.size() > 0) {
                if (currentAddedIdleTime == 0) currentAddedIdleTime = generator.nextFloat() * maximumAddedIdleTime;
                if (currentIdleTime > minimumIdleTime + currentAddedIdleTime) {
                    Object[] values = idleAnimationList.values().toArray();
                    Animation randomIdleAnimation = (Animation) values[generator.nextInt(values.length)];
                    runAnimation(randomIdleAnimation);
                }
            }
        }
        if (animationDirection == AnimationDirection.LEFT) sprite.setFlip(true, false);
        else if (animationDirection == AnimationDirection.RIGHT) sprite.setFlip(false, false);
    }

    public void stopAnimation() {
        currentAnimation = null;
        sprite.setFlip(false, false);
        currentIdleTime = 0;
        currentAddedIdleTime = 0;
    }

    public void setMinimumIdleTime(float minimumIdleTime) {
        this.minimumIdleTime = minimumIdleTime;
    }

    public void setMaximumAddedIdleTime(float maximumAddedIdleTime) {
        this.maximumAddedIdleTime = maximumAddedIdleTime;
    }

    public enum AnimationDirection {
        LEFT, RIGHT
    }
}
