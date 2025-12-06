package com.fernanda.finpro.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.fernanda.finpro.entities.Player;

public class InputHandler {

    public void handleInput(Player player) {
        // 0. CEK DODGE (PRIORITAS TERTINGGI)
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            player.dodge();
        }

        // Jika sedang Dodge, ignore input gerakan (agar dash-nya komit ke satu arah)
        // Logika gerak Dodge diurus penuh oleh Player.java
        if (player.isDodging()) return;

        // 1. CEK SERANGAN
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            player.attack();
        }

        float moveX = 0;
        float moveY = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY = -1;
        }

        // REVISI: Run & Attack Enabled
        // Kita langsung set velocity berdasarkan input, tanpa peduli apakah sedang attack atau tidak.
        player.velocity.set(moveX, moveY).nor().scl(150f);

        // Update arah hadap
        if (moveX > 0) player.facingRight = true;
        if (moveX < 0) player.facingRight = false;
    }
}
