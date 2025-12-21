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

public class Boss {

    public enum BossState { IDLE, ATTACKING, DYING }
    public enum AttackType { SMASH, METEOR }
    public enum SmashPhase { WINDUP, HOLD, RECOVER }

    private float width;
    private float height;
    public Vector2 position;

    private BossState currentState;
    private AttackType currentAttack;
    private SmashPhase smashPhase;

    private int maxHealth;
    private int currentHealth;
    private boolean isDead;
    private int attackDamage = 100;

    private float stateTimer;
    private float attackTimer;
    private float idleTimer;
    private float immunityTimer = 0;

    private Rectangle leftHandRect;
    private Rectangle rightHandRect;
    private boolean hasDealtImpactDamage;

    private final float HAND_OFFSET_X = 220f;
    private final float HAND_OFFSET_Y = 200f;
    private final float HAND_WIDTH = 240f;
    private final float HAND_HEIGHT = 190f;
    private final float IMPACT_DURATION = 0.25f;

    private MeteorController meteorController;
    private boolean isCasting;

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
        this.attackTimer = 0;
        this.maxHealth = 15;
        this.currentHealth = this.maxHealth;
        this.isDead = false;

        this.leftHandRect = new Rectangle(0, 0, HAND_WIDTH, HAND_HEIGHT);
        this.rightHandRect = new Rectangle(0, 0, HAND_WIDTH, HAND_HEIGHT);
        this.hasDealtImpactDamage = false;

        this.meteorController = new MeteorController();
        this.isCasting = false;

        initAnimation();
    }

    private void initAnimation() {
        this.idleAnim = createAnimation(GameAssetManager.BOSS_IDLE, 3, 0.15f, Animation.PlayMode.LOOP);
        this.attackAnim = createAnimation(GameAssetManager.BOSS_ATTACK, 6, 0.1f, Animation.PlayMode.NORMAL);
        this.hitboxAnim = createAnimation(GameAssetManager.BOSS_HITBOX, 3, 0.15f, Animation.PlayMode.LOOP);
        this.retrieveAnim = createAnimation(GameAssetManager.BOSS_RETRIEVE, 6, 0.1f, Animation.PlayMode.NORMAL);
        this.deathAnim = createAnimation(GameAssetManager.BOSS_DEATH, 8, 0.15f, Animation.PlayMode.NORMAL);

        try {
            this.castingAnim = createAnimation("Boss-Casting.png", 6, 0.15f, Animation.PlayMode.NORMAL);
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
        else if (currentState == BossState.ATTACKING) {

            if (currentAttack == AttackType.SMASH) {
                updateSmashAttack(dt);
            }
            else if (currentAttack == AttackType.METEOR) {
                attackTimer += dt;

                if (isCasting) {
                    if (castingAnim.isAnimationFinished(attackTimer)) {
                        meteorController.startRain();
                        isCasting = false;
                        currentState = BossState.IDLE;
                        System.out.println("Boss Casting Selesai -> Hujan Meteor Dimulai");
                    }
                }
            }
        }
    }

    private void updateHitboxPositions() {
        float handY = position.y - (height / 2) + HAND_OFFSET_Y;
        leftHandRect.setPosition(position.x - HAND_OFFSET_X - (HAND_WIDTH / 2), handY);
        rightHandRect.setPosition(position.x + HAND_OFFSET_X - (HAND_WIDTH / 2), handY);
    }

    private void pickRandomAttack() {
        int dice = MathUtils.random(1, 2);

        this.currentState = BossState.ATTACKING;
        this.attackTimer = 0;
        this.hasDealtImpactDamage = false;

        if (dice == 1) {
            this.currentAttack = AttackType.SMASH;
            this.smashPhase = SmashPhase.WINDUP;
        } else {
            this.currentAttack = AttackType.METEOR;
            this.isCasting = true;
        }
    }

    private void updateSmashAttack(float dt) {
        attackTimer += dt;
        switch (smashPhase) {
            case WINDUP:
                if (attackAnim.isAnimationFinished(attackTimer)) {
                    smashPhase = SmashPhase.HOLD;
                    attackTimer = 0;
                    hasDealtImpactDamage = false;
                }
                break;
            case HOLD:
                if (attackTimer > 5.0f) {
                    smashPhase = SmashPhase.RECOVER;
                    attackTimer = 0;
                }
                break;
            case RECOVER:
                if (retrieveAnim.isAnimationFinished(attackTimer)) {
                    currentState = BossState.IDLE;
                }
                break;
        }
    }

    public void checkSmashCollision(Player player) {
        if (isDead) return;
        if (currentState == BossState.ATTACKING && smashPhase == SmashPhase.HOLD && attackTimer <= IMPACT_DURATION) {
            if (!hasDealtImpactDamage) {
                if (leftHandRect.overlaps(player.getHitbox()) || rightHandRect.overlaps(player.getHitbox())) {
                    player.takeDamage(attackDamage);
                    hasDealtImpactDamage = true;
                }
            }
        }
    }

    public boolean checkHitByPlayer(Rectangle playerAttackBox, int damage) {
        if (isDead) return false;
        if (immunityTimer > 0) return false;

        if (currentState == BossState.ATTACKING && smashPhase == SmashPhase.HOLD && attackTimer > IMPACT_DURATION) {
            if (leftHandRect.overlaps(playerAttackBox) || rightHandRect.overlaps(playerAttackBox)) {
                takeDamage(damage);
                immunityTimer = 0.5f;
                return true;
            }
        }
        return false;
    }

    public void takeDamage(int damage) {
        if (isDead || currentState == BossState.DYING) return; // Cegah damage saat sudah mati/sekarat

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
        if (currentState == BossState.ATTACKING && currentAttack == AttackType.SMASH && smashPhase == SmashPhase.HOLD) {
            if (attackTimer <= IMPACT_DURATION) {
                shapeRenderer.setColor(Color.RED);
            } else {
                shapeRenderer.setColor(Color.GREEN);
            }
            shapeRenderer.rect(leftHandRect.x, leftHandRect.y, leftHandRect.width, leftHandRect.height);
            shapeRenderer.rect(rightHandRect.x, rightHandRect.y, rightHandRect.width, rightHandRect.height);
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
        }else if (currentState == BossState.ATTACKING) {
            if (currentAttack == AttackType.SMASH) {
                switch (smashPhase) {
                    case WINDUP: currentFrame = attackAnim.getKeyFrame(attackTimer, false); break;
                    case HOLD: currentFrame = hitboxAnim.getKeyFrame(attackTimer, true); break;
                    case RECOVER: currentFrame = retrieveAnim.getKeyFrame(attackTimer, false); break;
                }
            }
            else if (currentAttack == AttackType.METEOR) {
                if (isCasting && castingAnim != null) {
                    currentFrame = castingAnim.getKeyFrame(attackTimer, false);
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
            currentAttack == AttackType.SMASH && smashPhase == SmashPhase.HOLD;
    }

    public void reset() {
        this.currentHealth = this.maxHealth;
        this.isDead = false;
        this.currentState = BossState.IDLE;

        this.stateTimer = 0;
        this.attackTimer = 0;
        this.idleTimer = 0;
        this.immunityTimer = 0;

        this.hasDealtImpactDamage = false;
        this.isCasting = false;

        // Reset Meteor Controller juga
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
