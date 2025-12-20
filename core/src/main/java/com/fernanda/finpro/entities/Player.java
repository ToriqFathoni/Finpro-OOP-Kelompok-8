package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic. gdx.graphics.g2d.TextureRegion;
import com.badlogic. gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.components.Inventory;
import com.fernanda.finpro.components.PlayerStats;
import com.fernanda. finpro.input.InputHandler;
import com.fernanda. finpro.states.*;

public class Player {
    // --- FISIKA & POSISI ---
    public Vector2 position;
    public Vector2 velocity;
    public boolean facingRight = true;

    // --- COMPONENTS ---
    public PlayerStats stats;
    public Inventory inventory;

    // STATS
    private int attackDamage = 15;

    // CONFIG BADAN (LOGIKA)
    private static final int LOGICAL_WIDTH = 15;
    private static final int LOGICAL_HEIGHT = 20;

    // --- CONFIG VISUAL (GAMBAR) ---
    private static final float DRAW_OFFSET_X = 0f;
    private static final float DRAW_OFFSET_Y = 0f;

    // --- CONFIG HITBOX SERANGAN ---
    private static final float ATTACK_WIDTH = 18f;
    private static final float ATTACK_HEIGHT = 25f;
    private static final float ATTACK_OFFSET_Y = 0f;

    // Timing Hitbox Serangan
    private static final float DAMAGE_START_TIME = 0.2f;
    private static final float DAMAGE_END_TIME   = 0.4f;

    private Rectangle attackRect = new Rectangle();

    // --- COOLDOWN & TIMERS ---
    private float attackTimer = 0f;
    private final float ATTACK_COOLDOWN = 0.5f;

    // --- DODGE CONFIG ---
    private DodgeState dodgeState;
    private float dodgeTimer = 0f;
    private float dodgeCooldownTimer = 0f;

    private final float DODGE_DURATION = 0.2f;
    private final float DODGE_SPEED = 300f;
    private final float DODGE_COST = 20f;
    private final float DODGE_WAIT_TIME = 0.3f;

    // Timer invincibility setelah kena hit
    private float invincibilityTimer = 0f;
    private final float INVINCIBILITY_DURATION = 1.0f;

    private Vector2 dodgeDirection = new Vector2();
    
    // --- DAMAGE BOOST BUFF (ORC ELIXIR) ---
    private float damageBoostTimer = 0f;
    private final float DAMAGE_BOOST_DURATION = 120f; // 2 minutes
    private final float DAMAGE_BOOST_MULTIPLIER = 1.5f; // 50% damage increase

    // --- STATE MANAGEMENT ---
    private PlayerState currentState;
    private PlayerState idleState;
    private PlayerState walkState;
    private AttackState attackState;
    private HurtState hurtState;
    private DeathState deathState;

    private InputHandler inputHandler;
    private float stateTime;

    public Player(float startX, float startY) {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0);
        this.inputHandler = new InputHandler();

        this.idleState = new IdleState();
        this.walkState = new WalkState();
        this.attackState = new AttackState();
        this.dodgeState = new DodgeState();
        this.hurtState = new HurtState();
        this.deathState = new DeathState();

        this.currentState = idleState;
        this.stateTime = 0f;

        // Inisialisasi Components
        this.stats = new PlayerStats(50f, 100f, 1.0f, 15f);
        this.inventory = new Inventory();
    }

    public void update(float dt) {
        stats.update(dt);
        
        // Update damage boost timer
        if (damageBoostTimer > 0) {
            damageBoostTimer -= dt;
            if (damageBoostTimer <= 0) {
                damageBoostTimer = 0;
                System.out.println("[BUFF] Damage Boost expired!");
            }
        }

        if (invincibilityTimer > 0) {
            invincibilityTimer -= dt;
        }

        if (attackTimer > 0) attackTimer -= dt;

        if (dodgeCooldownTimer > 0) {
            dodgeCooldownTimer -= dt;
        }

        if (currentState == deathState) {
            stateTime += dt;
            velocity.set(0, 0);
            return;
        }

        inputHandler.handleInput(this);

        if (currentState == dodgeState) {
            velocity.set(dodgeDirection).scl(DODGE_SPEED);
            dodgeTimer -= dt;
            if (dodgeTimer <= 0) {
                velocity.set(0, 0);
                changeState(idleState);
                dodgeCooldownTimer = DODGE_WAIT_TIME;
                System.out.println("Dodge Selesai.  Masuk Cooldown:  " + DODGE_WAIT_TIME + " detik.");
            }
        }

        position.mulAdd(velocity, dt);

        if (velocity.x > 0) {
            facingRight = true;
        } else if (velocity. x < 0) {
            facingRight = false;
        }

        if (currentState == dodgeState) {
            // Tunggu logika di atas
        }
        else if (currentState == attackState) {
            if (attackState.isFinished(stateTime)) {
                changeState(idleState);
            }
        }
        else if (currentState == hurtState) {
            if (hurtState.isFinished(stateTime)) {
                changeState(idleState);
            }
        }
        else {
            if (velocity.len2() > 0) {
                if (currentState != walkState) {
                    changeState(walkState);
                }
            }
            else {
                if (currentState != idleState) {
                    changeState(idleState);
                }
            }
        }

        stateTime += dt;
        currentState.update(dt);
    }

    public void dodge() {
        if (currentState == dodgeState || currentState == attackState) return;

        if (dodgeCooldownTimer > 0) {
            System.out.println("Gagal Dodge!  Sedang Cooldown.  Sisa: " + String.format("%.2f", dodgeCooldownTimer));
            return;
        }

        if (stats.useStamina(DODGE_COST)) {
            System.out.println("Dodge Dimulai!");
            changeState(dodgeState);
            dodgeTimer = DODGE_DURATION;

            if (velocity.len2() > 0) {
                dodgeDirection.set(velocity).nor();
            } else {
                dodgeDirection. set(facingRight ? 1 : -1, 0);
            }
        } else {
            System.out. println("Stamina tidak cukup!");
        }
    }

    public void attack() {
        if (attackTimer > 0) return;
        if (currentState == dodgeState) return;

        if (currentState != attackState) {
            performAttack();
        }
        else if (currentState == attackState && attackState.isFinished(stateTime)) {
            performAttack();
        }
    }

    private void performAttack() {
        changeState(attackState);
        attackTimer = ATTACK_COOLDOWN;
    }

    public void takeDamage(float amount) {
        if (currentState == dodgeState) {
            System. out.println("DODGED!  (Damage diabaikan karena rolling)");
            return;
        }

        if (invincibilityTimer > 0) {
            return;
        }

        stats.takeDamage(amount);

        if (stats.getCurrentHealth() <= 0) {
            if (currentState != deathState) {
                changeState(deathState);
                velocity.set(0, 0);
                System.out.println("Player Mati -> Masuk DeathState");
            }
        } else {
            changeState(hurtState);
            System.out.println("Player Luka -> Masuk HurtState");
        }

        invincibilityTimer = INVINCIBILITY_DURATION;
        System.out.println("Player terkena hit!  Kebal aktif selama " + INVINCIBILITY_DURATION + "s");
    }

    public Rectangle getAttackHitbox() {
        float x = position.x;
        float y = position.y + ATTACK_OFFSET_Y;
        if (facingRight) x += LOGICAL_WIDTH;
        else x -= ATTACK_WIDTH;

        attackRect.set(x, y, ATTACK_WIDTH, ATTACK_HEIGHT);
        return attackRect;
    }

    public Rectangle getHitbox() {
        return new Rectangle(position.x, position. y, LOGICAL_WIDTH, LOGICAL_HEIGHT);
    }

    public boolean isHitboxActive() {
        if (currentState != attackState) return false;
        return stateTime >= DAMAGE_START_TIME && stateTime <= DAMAGE_END_TIME;
    }

    public boolean isAttacking() { return currentState == attackState; }
    public boolean isDodging() { return currentState == dodgeState; }

    private void changeState(PlayerState newState) {
        currentState = newState;
        currentState.enter();
        stateTime = 0;
    }

    public float getWidth() { return LOGICAL_WIDTH; }
    public float getHeight() { return LOGICAL_HEIGHT; }
    public int getDamage() {
        float finalDamage = attackDamage;
        if (damageBoostTimer > 0) {
            finalDamage *= DAMAGE_BOOST_MULTIPLIER;
        }
        return (int)finalDamage;
    }
    public void setDamage(int amount) {
        this.attackDamage = amount;
    }
    
    public void activateDamageBoost() {
        damageBoostTimer = DAMAGE_BOOST_DURATION;
        System.out.println("[BUFF] Damage Boost activated! (2 minutes, +50% damage)");
    }
    
    public float getDamageBoostTimer() {
        return damageBoostTimer;
    }
    
    public boolean hasDamageBoost() {
        return damageBoostTimer > 0;
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        if (currentFrame != null) {
            if (currentState == dodgeState) {
                batch. setColor(0.5f, 0.5f, 1f, 0.7f);
            } else {
                batch.setColor(Color.WHITE);
            }

            boolean isFlipX = currentFrame.isFlipX();
            if ((! facingRight && ! isFlipX) || (facingRight && isFlipX)) {
                currentFrame. flip(true, false);
            }

            float textureWidth = currentFrame.getRegionWidth();
            float textureHeight = currentFrame.getRegionHeight();

            float rawCenterX = (LOGICAL_WIDTH - textureWidth) / 2f;
            float rawCenterY = (LOGICAL_HEIGHT - textureHeight) / 2f;

            if (textureWidth % 2 != 0) rawCenterX += 0.5f;
            if (textureHeight % 2 != 0) rawCenterY += 0.5f;

            int fixedCenterX = (int) rawCenterX;
            int fixedCenterY = (int) rawCenterY;

            float drawX = position.x + fixedCenterX + DRAW_OFFSET_X;
            float drawY = position. y + fixedCenterY + DRAW_OFFSET_Y;

            batch.draw(currentFrame, (int)drawX, (int)drawY);
            batch.setColor(Color.WHITE);
        }
    }

    public void reset(float startX, float startY) {
        this.position.set(startX, startY);
        this.velocity.set(0, 0);
        this.facingRight = true;

        this.stats.reset();
        this.inventory.clear();

        this.invincibilityTimer = 0f;
        this.attackTimer = 0f;
        this. dodgeCooldownTimer = 0f;

        changeState(idleState);

        System.out.println("Player Reset:  HP Penuh, State kembali ke Idle.");
    }

    public boolean isDeathAnimationFinished() {
        return currentState == deathState && stateTime > 1.0f;
    }
}