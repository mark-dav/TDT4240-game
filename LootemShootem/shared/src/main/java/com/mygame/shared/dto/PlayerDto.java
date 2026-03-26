package com.mygame.shared.dto;

import com.mygame.shared.util.Vec2;

public final class PlayerDto {
    public String playerId;
    public String username;

    public Vec2 pos;
    public Vec2 vel;
    public Vec2 facing;

    public float hp;
    public float moveSpeed;

    public WeaponType equippedWeaponType;
    public int equippedAmmo;

    public int score;
    public float timeSurvived;
    public boolean isDead;
    public float respawnTimer;
    public float speedBoostTimer;

    // Secondary weapon slot (null = empty)
    public WeaponType secondaryWeaponType;
    public int secondaryAmmo;

    public String lastPickupNotice;

    public PlayerDto() {}

    public PlayerDto(String playerId, String username, Vec2 pos, Vec2 vel, Vec2 facing,
                     float hp, float moveSpeed, WeaponType equippedWeaponType, int equippedAmmo) {
        this.playerId = playerId;
        this.username = username;
        this.pos = pos;
        this.vel = vel;
        this.facing = facing;
        this.hp = hp;
        this.moveSpeed = moveSpeed;
        this.equippedWeaponType = equippedWeaponType;
        this.equippedAmmo = equippedAmmo;
    }
}