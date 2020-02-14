/**
 * (c) 2017 dmulloy2
 */
package net.dmulloy2.shadowperms.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author dmulloy2
 */
@Getter
@AllArgsConstructor
public class Tuple<A, B>
{
	private final A first;
	private final B second;
}