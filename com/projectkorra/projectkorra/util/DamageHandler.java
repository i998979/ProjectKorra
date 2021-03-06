package com.projectkorra.projectkorra.util;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.event.AbilityDamageEntityEvent;
import com.projectkorra.projectkorra.event.EntityBendingDeathEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.metadata.FixedMetadataValue;

public class DamageHandler {

	/**
	 * Damages an Entity by amount of damage specified. Starts a
	 * {@link EntityDamageByEntityEvent}.
	 * 
	 * @param ability The ability that is used to damage the entity
	 * @param entity The entity that is receiving the damage
	 * @param damage The amount of damage to deal
	 */
	@SuppressWarnings("deprecation")
	public static void damageEntity(Entity entity, Player source, double damage, Ability ability, boolean ignoreArmor) {
		if (TempArmor.hasTempArmor((LivingEntity) entity)) {
			ignoreArmor = true;
		}
		if (ability == null) {
			return;
		}
		if (source == null) {
			source = ability.getPlayer();
		}

		AbilityDamageEntityEvent damageEvent = new AbilityDamageEntityEvent(entity, ability, damage, ignoreArmor);
		Bukkit.getServer().getPluginManager().callEvent(damageEvent);
		if (entity instanceof LivingEntity) {
			if (entity instanceof Player && Commands.invincible.contains(entity.getName())) {
				damageEvent.setCancelled(true);
			}
			if (!damageEvent.isCancelled()) {
				damage = damageEvent.getDamage();
				if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus") && source != null) {
					NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_REACH);
					NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_DIRECTION);
					NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_NOSWING);
					NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_SPEED);
					NCPExemptionManager.exemptPermanently(source, CheckType.COMBINED_IMPROBABLE);
					NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_SELFHIT);
				}

				if (((LivingEntity) entity).getHealth() - damage <= 0 && !entity.isDead()) {
					EntityBendingDeathEvent event = new EntityBendingDeathEvent(entity, damage, ability);
					Bukkit.getServer().getPluginManager().callEvent(event);
				}

				EntityDamageByEntityEvent finalEvent = new EntityDamageByEntityEvent(source, entity, DamageCause.CUSTOM, damage);
				((LivingEntity) entity).damage(damage, source);
				entity.setLastDamageCause(finalEvent);
				
				// Modified by i998979
				Bukkit.broadcastMessage(ChatColor.GREEN + "Damage Cause: " + entity.getLastDamageCause().getCause());
				source.setMetadata("PK.DamageHandler", new FixedMetadataValue(ProjectKorra.plugin, true));
				// Modified by i998979
				
				if (ignoreArmor) {
				    if (finalEvent.isApplicable(DamageModifier.ARMOR)) {
				        finalEvent.setDamage(DamageModifier.ARMOR, 0);
				    }
				}

				if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus") && source != null) {
					NCPExemptionManager.unexempt(source, CheckType.FIGHT_REACH);
					NCPExemptionManager.unexempt(source, CheckType.FIGHT_DIRECTION);
					NCPExemptionManager.unexempt(source, CheckType.FIGHT_NOSWING);
					NCPExemptionManager.unexempt(source, CheckType.FIGHT_SPEED);
					NCPExemptionManager.unexempt(source, CheckType.COMBINED_IMPROBABLE);
					NCPExemptionManager.unexempt(source, CheckType.FIGHT_SELFHIT);
				}
			}
		}

	}

	public static void damageEntity(Entity entity, Player source, double damage, Ability ability) {
		damageEntity(entity, source, damage, ability, true);
	}

	public static void damageEntity(Entity entity, double damage, Ability ability) {
		damageEntity(entity, ability.getPlayer(), damage, ability);
	}
}
