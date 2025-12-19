package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.singleton.GameAssetManager;

import java.util.ArrayList;
import java.util.List;

public class Boss {

    // --- ENUMS FOR STATE MACHINE ---
    public enum BossState {
        IDLE,
        ATTACKING
    }

    public enum AttackType {
        SMASH,
        TYPE_B,     // Placeholder
        TYPE_C      // Placeholder
    }

    public enum SmashPhase {
        WINDUP,
        HOLD,
        RECOVER
    }

    // Ukuran Boss (akan dihitung otomatis agar tidak gepeng/stretched)
    private float width;
    private float height;

    public Vector2 position;

    private BossState currentState;
    private AttackType currentAttack;
    private SmashPhase smashPhase;

    // --- STATS ---
    private int maxHealth;
    private int currentHealth;
    private boolean isDead;

    private float stateTimer;       // Timer ntuk animasi
    private float attackTimer;      // Timer durasi fase serangan
    private float idleTimer;        // Timer jeda antar serangan

    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> attackAnim;
    private Animation<TextureRegion> hitboxAnim;
    private Animation<TextureRegion> retrieveAnim;

    public Boss(float x, float y) {
        // Posisikan boss (titik X,Y adalah PUSAT boss)
        this.position = new Vector2(x, y);
        this.stateTimer = 0;

        // Initialize State
        this.currentState = BossState.IDLE;
        this.idleTimer = 0;
        this.attackTimer = 0;

        // Initialize Stats
        this.maxHealth = 500;
        this.currentHealth = this.maxHealth;
        this.isDead = false;

        initAnimation();
    }

    private void initAnimation() {
        this.idleAnim = createAnimation(GameAssetManager.BOSS_IDLE, 3, 0.15f, Animation.PlayMode.LOOP);

        this.attackAnim = createAnimation(GameAssetManager.BOSS_ATTACK, 6, 0.1f, Animation.PlayMode.NORMAL);

        this.hitboxAnim = createAnimation(GameAssetManager.BOSS_HITBOX, 3, 0.15f, Animation.PlayMode.LOOP);

        this.retrieveAnim = createAnimation(GameAssetManager.BOSS_RETRIEVE, 6, 0.1f, Animation.PlayMode.NORMAL);

        this.width = 1400f;

        TextureRegion firstFrame = this.idleAnim.getKeyFrame(0);
        float aspectRatio = (float) firstFrame.getRegionWidth() / firstFrame.getRegionHeight();

        this.height = this.width / aspectRatio;
    }

    private Animation<TextureRegion> createAnimation(String assetName, int cols, float frameDuration, Animation.PlayMode mode) {
        Texture texture = GameAssetManager.getInstance().getTexture(assetName);
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

    public void update(float dt) {
        // Update timer agar animasi bergerak
        stateTimer += dt;

        if (currentState == BossState.IDLE) {
            idleTimer += dt;
            if (idleTimer > 4.0f) {
                pickRandomAttack();
                idleTimer = 0;
            }
        }
        else if (currentState == BossState.ATTACKING) {
            switch (currentAttack) {
                case SMASH:
                    updateSmashAttack(dt);
                    break;
                case TYPE_B:
                    // Placeholder
                    break;
                case TYPE_C:
                    // Placeholder
                    break;
            }
        }
    }

    private void pickRandomAttack() {
        List<AttackType> availableAttacks = new ArrayList<>();
        availableAttacks.add(AttackType.SMASH);
        // availableAttacks.add(AttackType.TYPE_B);

        int index = MathUtils.random(0, availableAttacks.size() - 1);
        this.currentAttack = availableAttacks.get(index);

        this.currentState = BossState.ATTACKING;
        this.attackTimer = 0;

        if (this.currentAttack == AttackType.SMASH) {
            this.smashPhase = SmashPhase.WINDUP;
        }

        System.out.println("Boss Attack: " + this.currentAttack);
    }

    private void updateSmashAttack(float dt) {
        attackTimer += dt;

        switch (smashPhase) {
            case WINDUP:
                if (attackAnim.isAnimationFinished(attackTimer)) {
                    smashPhase = SmashPhase.HOLD;
                    attackTimer = 0;
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

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = null;

        if (currentState == BossState.IDLE) {
            currentFrame = idleAnim.getKeyFrame(stateTimer, true);
        }
        else if (currentState == BossState.ATTACKING) {
            if (currentAttack == AttackType.SMASH) {
                switch (smashPhase) {
                    case WINDUP:
                        currentFrame = attackAnim.getKeyFrame(attackTimer, false);
                        break;
                    case HOLD:
                        currentFrame = hitboxAnim.getKeyFrame(attackTimer, true);
                        break;
                    case RECOVER:
                        currentFrame = retrieveAnim.getKeyFrame(attackTimer, false);
                        break;
                }
            }
        }

        if (currentFrame != null) {
            batch.draw(currentFrame,
                position.x - width / 2,
                position.y - height / 2,
                width, height);
        }
    }

    public void takeDamage(int damage) {
        if (isDead) return;

        this.currentHealth -= damage;
        if (this.currentHealth <= 0) {
            this.currentHealth = 0;
            this.isDead = true;
            // TODO: Trigger death animation or logic here
        }
    }

    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isDead() { return isDead; }
}
