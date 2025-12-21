package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.singleton.GameAssetManager;
import com.fernanda.finpro.strategy.AttackStrategy;
import com.fernanda.finpro.strategy.SmashAttackStrategy;
import com.fernanda.finpro.strategy.MeteorAttackStrategy;

public class Boss {

    public enum BossState { IDLE, ATTACKING, DYING }

    private float width;
    private float height;
    public Vector2 position;

    private BossState currentState;

    private AttackStrategy currentStrategy;
    private SmashAttackStrategy smashStrategy;
    private MeteorAttackStrategy meteorStrategy;

    private int maxHealth;
    private int currentHealth;
    private boolean isDead;
    private int attackDamage = 100;

    private float stateTimer;
    private float idleTimer;
    private float immunityTimer = 0;

    private Rectangle leftHandRect;
    private Rectangle rightHandRect;

    private final float HAND_OFFSET_X = 220f;
    private final float HAND_OFFSET_Y = 200f;
    private final float HAND_WIDTH = 240f;
    private final float HAND_HEIGHT = 190f;

    private MeteorController meteorController;

    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> attackAnim;
    private Animation<TextureRegion> hitboxAnim;
    private Animation<TextureRegion> retrieveAnim;
    private Animation<TextureRegion> deathAnim;
    private Animation<TextureRegion> castingAnim;

    public Boss(float x, float y) {
        this.position = new Vector2(x, y);
        this.stateTimer = 0;
        this.currentState = BossState.IDLE;
        this.idleTimer = 0;
        this.maxHealth = 1; // SEMENTARA: HP Boss jadi 1 untuk testing
        this.currentHealth = this.maxHealth;
        this.isDead = false;

        this.leftHandRect = new Rectangle(0, 0, HAND_WIDTH, HAND_HEIGHT);
        this.rightHandRect = new Rectangle(0, 0, HAND_WIDTH, HAND_HEIGHT);

        this.meteorController = new MeteorController();

        this.smashStrategy = new SmashAttackStrategy();
        this.meteorStrategy = new MeteorAttackStrategy(meteorController);
        this.currentStrategy = null;

        initAnimation();
    }

    private void initAnimation() {
        this.idleAnim = createAnimation(GameAssetManager.BOSS_IDLE, 3, 0.15f, Animation.PlayMode.LOOP);
        this.attackAnim = createAnimation(GameAssetManager.BOSS_ATTACK, 6, 0.1f, Animation.PlayMode.NORMAL);
        this.hitboxAnim = createAnimation(GameAssetManager.BOSS_HITBOX, 3, 0.15f, Animation.PlayMode.LOOP);
        this.retrieveAnim = createAnimation(GameAssetManager.BOSS_RETRIEVE, 6, 0.1f, Animation.PlayMode.NORMAL);
        this.deathAnim = createAnimation(GameAssetManager.BOSS_DEATH, 8, 0.15f, Animation.PlayMode.NORMAL);

        try {
            this.castingAnim = createAnimation(GameAssetManager.BOSS_CASTING, 6, 0.15f, Animation.PlayMode.NORMAL);
        } catch (Exception e) {
            System.err.println("Gagal load Boss-Casting, pastikan asset sudah di-load di GameAssetManager");
            this.castingAnim = this.attackAnim;
        }

        this.width = 1400f;
        TextureRegion firstFrame = this.idleAnim.getKeyFrame(0);
        float aspectRatio = (float) firstFrame.getRegionWidth() / firstFrame.getRegionHeight();
        this.height = this.width / aspectRatio;
    }

    private Animation<TextureRegion> createAnimation(String assetName, int cols, float frameDuration, Animation.PlayMode mode) {
        Texture texture = GameAssetManager.getInstance().getTexture(assetName);
        if (texture == null) return null;

        int frameWidth = texture.getWidth() / cols;
        int frameHeight = texture.getHeight();
        TextureRegion[][] tmp = TextureRegion.split(texture, frameWidth, frameHeight);

        TextureRegion[] frames = new TextureRegion[cols];
        for (int i = 0; i < cols; i++) {
            frames[i] = tmp[0][i];
        }

        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    public void update(float dt, Player player) {
        if (isDead) return;

        stateTimer += dt;
        if (immunityTimer > 0) immunityTimer -= dt;

        if (currentState == BossState.DYING) {
            if (deathAnim.isAnimationFinished(stateTimer)) {
                isDead = true;
                System.out.println("BOSS DEFEATED (Animation Finished)");
            }
            return;
        }

        updateHitboxPositions();
        meteorController.update(dt, player);

        if (currentState == BossState.IDLE) {
            idleTimer += dt;
            if (idleTimer > 3.0f) {
                pickRandomAttack();
                idleTimer = 0;
            }
        }
        else if (currentState == BossState.ATTACKING && currentStrategy != null) {
            currentStrategy.execute(this, player, dt);

            if (currentStrategy.isFinished()) {
                currentState = BossState.IDLE;
                currentStrategy = null;
            }
        }
    }

    private void updateHitboxPositions() {
        float handY = position.y - (height / 2) + HAND_OFFSET_Y;
        leftHandRect.setPosition(position.x - HAND_OFFSET_X - (HAND_WIDTH / 2), handY);
        rightHandRect.setPosition(position.x + HAND_OFFSET_X - (HAND_WIDTH / 2), handY);
    }

    /**
     * Strategy Pattern - Pick and set random attack strategy
     */
    private void pickRandomAttack() {
        int dice = MathUtils.random(1, 2);

        this.currentState = BossState.ATTACKING;

        if (dice == 1) {
            // Use Smash Attack Strategy
            smashStrategy.reset();
            currentStrategy = smashStrategy;
            System.out.println("Boss using: " + currentStrategy.getName());
        } else {
            // Use Meteor Attack Strategy
            meteorStrategy.reset();
            currentStrategy = meteorStrategy;
            System.out.println("Boss using: " + currentStrategy.getName());
        }
    }

    public void checkSmashCollision(Player player) {
        if (isDead) return;
        if (leftHandRect.overlaps(player.getHitbox()) || rightHandRect.overlaps(player.getHitbox())) {
            player.takeDamage(attackDamage);
            System.out.println("Boss Smash Hit! Player took " + attackDamage + " damage");
        }
    }

    public boolean checkHitByPlayer(Rectangle playerAttackBox, int damage) {
        if (isDead) return false;
        if (immunityTimer > 0) return false;

        // Check if using smash strategy and in vulnerable phase
        if (currentState == BossState.ATTACKING && currentStrategy == smashStrategy) {
            if (smashStrategy.canBeHit()) {
                if (leftHandRect.overlaps(playerAttackBox) || rightHandRect.overlaps(playerAttackBox)) {
                    takeDamage(damage);
                    immunityTimer = 0.5f;
                    return true;
                }
            }
        }
        return false;
    }

    public void takeDamage(int damage) {
        if (isDead || currentState == BossState.DYING) return;

        this.currentHealth -= damage;
        if (this.currentHealth <= 0) {
            this.currentHealth = 0;

            this.currentState = BossState.DYING;
            this.stateTimer = 0;

            System.out.println("BOSS DYING...");
        }
    }

    public void renderDebug(ShapeRenderer shapeRenderer) {
        /*
        if (isDead) return;

        // Render hand hitboxes during smash attack
        if (currentState == BossState.ATTACKING && currentStrategy == smashStrategy) {
            SmashAttackStrategy.Phase phase = smashStrategy.getCurrentPhase();
            if (phase == SmashAttackStrategy.Phase.HOLD) {
                if (smashStrategy.canBeHit()) {
                    shapeRenderer.setColor(Color.GREEN);
                } else {
                    shapeRenderer.setColor(Color.RED);
                }
                shapeRenderer.rect(leftHandRect.x, leftHandRect.y, leftHandRect.width, leftHandRect.height);
                shapeRenderer.rect(rightHandRect.x, rightHandRect.y, rightHandRect.width, rightHandRect.height);
            }
        }

        meteorController.renderDebug(shapeRenderer);

         */
    }

    public void render(SpriteBatch batch) {
        if (isDead) return;
        TextureRegion currentFrame = null;

        if (currentState == BossState.IDLE) {
            currentFrame = idleAnim.getKeyFrame(stateTimer, true);
        } else if (currentState == BossState.DYING) {
            currentFrame = deathAnim.getKeyFrame(stateTimer, false);
        } else if (currentState == BossState.ATTACKING && currentStrategy != null) {
            // Strategy Pattern - Get animation based on current strategy
            if (currentStrategy == smashStrategy) {
                SmashAttackStrategy.Phase phase = smashStrategy.getCurrentPhase();
                float timer = smashStrategy.getPhaseTimer();

                switch (phase) {
                    case WINDUP: currentFrame = attackAnim.getKeyFrame(timer, false); break;
                    case HOLD: currentFrame = hitboxAnim.getKeyFrame(timer, true); break;
                    case RECOVER: currentFrame = retrieveAnim.getKeyFrame(timer, false); break;
                }
            } else if (currentStrategy == meteorStrategy) {
                if (meteorStrategy.isCasting() && castingAnim != null) {
                    currentFrame = castingAnim.getKeyFrame(meteorStrategy.getCastingTimer(), false);
                } else {
                    currentFrame = idleAnim.getKeyFrame(stateTimer, true);
                }
            }
        }

        if (currentFrame != null) {
            batch.draw(currentFrame, position.x - width / 2, position.y - height / 2, width, height);
        }

        meteorController.render(batch);
    }

    public boolean isHandSolid() {
        return !isDead && currentState == BossState.ATTACKING &&
            currentStrategy == smashStrategy && smashStrategy.getCurrentPhase() == SmashAttackStrategy.Phase.HOLD;
    }

    public void reset() {
        this.currentHealth = this.maxHealth;
        this.isDead = false;
        this.currentState = BossState.IDLE;

        this.stateTimer = 0;
        this.idleTimer = 0;
        this.immunityTimer = 0;

        // Reset strategies
        this.currentStrategy = null;
        this.smashStrategy.reset();
        this.meteorStrategy.reset();

        // Reset Meteor Controller
        if (this.meteorController != null) {
            this.meteorController.reset();
        }

        updateHitboxPositions();

        System.out.println("Boss Reset: HP Full, State IDLE");
    }

    public Rectangle getLeftHandRect() { return leftHandRect; }
    public Rectangle getRightHandRect() { return rightHandRect; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isDead() { return isDead; }
}
