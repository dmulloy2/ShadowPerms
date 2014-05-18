/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author dmulloy2
 */

@Getter
@AllArgsConstructor
public enum Permission
{
	// Permission Management
	USER_ADD_SUBGROUP("user.add.subgroup"),
	USER_ADD_PERMISSION("user.add.permission"),
	USER_VIEW_INFO("user.view.info"),
	USER_HAS_GROUP("user.has.group"),
	USER_HAS_OPTION("user.has.option"),
	USER_HAS_PERMISSION("user.has.permission"),
	USER_LIST_PERMISSIONS("user.list.permissions"),
	USER_REMOVE_SUBGROUP("user.remove.subgroup"),
	USER_REMOVE_PERMISSION("user.remove.permission"),
	USER_SET_GROUP("user.set.group"),
	USER_SET_OPTION("user.set.option"),
	USER_SET_PREFIX("user.set.prefix"),
	USER_SET_SUFFIX("user.set.suffix"),

	GROUP_ADD_PERMISSION("group.add.permission"),
	GROUP_VIEW_INFO("group.view.info"),
	GROUP_CREATE("group.create"),
	GROUP_HAS_OPTION("group.has.option"),
	GROUP_HAS_PERMISSION("group.has.permission"),
	GROUP_LIST("group.list"),
	GROUP_LIST_PERMISSIONS("group.list.permissions"),
	GROUP_LIST_USERS("group.list.users"),
	GROUP_REMOVE_PERMISSION("group.remove.permission"),
	GROUP_SET_OPTION("group.set.option"),
	GROUP_SET_PREFIX("group.set.prefix"),

	// Chat
	CHAT_COLOR("chat.color"),
	CHAT_FORMATTING("chat.formatting"),
	CHAT_RAINBOW("chat.rainbow"),

	// Prefixes
	CMD_PREFIX("cmd.prefix"),
	CMD_PREFIX_RESET("cmd.prefix.reset"),
	CMD_PREFIX_RESET_OTHERS("cmd.prefix.reset.others"),

	// Suffixes
	CMD_SUFFIX("cmd.suffix"),
	CMD_SUFFIX_RESET("cmd.suffix.reset"),
	CMD_SUFFIX_RESET_OTHERS("cmd.suffix.reset.others"),

	// Other Commands
	CMD_RELOAD("cmd.reload"),
	CMD_VERSION("cmd.version");

	private final String node;
}