package de.cuuky.varo.world.border;

import java.util.ArrayList;

import org.bukkit.Bukkit;

import de.cuuky.varo.Main;
import de.cuuky.varo.config.config.ConfigEntry;
import de.cuuky.varo.config.messages.ConfigMessages;

public class BorderDecrease {

	private static ArrayList<BorderDecrease> decreases;
	private static boolean running;

	static {
		decreases = new ArrayList<>();
		running = false;

		startShrinking();
	}

	private double amount;
	private double bps;
	private Runnable startHook;
	private Runnable finishHook;

	public BorderDecrease(double amount, double bps) {
		this.amount = amount;
		this.bps = bps;

		decreases.add(this);
	}

	private static void startShrinking() {
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(Main.getInstance(), new Runnable() {

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				if (running)
					return;

				for (int i = 0; i < decreases.size(); i++) {
					running = true;
					decreases.get(i).shrink();
				}

				running = false;
			}
		}, 1, 20);
	}

	private void waitForBorder(double d) {
		try {
			Thread.sleep((long) (d * 1000) + 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void shrink() {
		VaroBorder border = VaroBorder.getInstance();

		int minsize = ConfigEntry.MIN_BORDER_SIZE.getValueAsInt();
		double size = border.getBorderSize(null);
		if (size <= minsize) {
			Bukkit.broadcastMessage(ConfigMessages.BORDER_MINIMUM_REACHED.getValue());
			remove();
			return;
		}

		startHook.run();
		if (minsize > 0)
			if ((int) (size - amount) < minsize) {
				border.setBorderSize(minsize, (long) ((size - minsize) / bps), null);
				waitForBorder((size - minsize) / bps);
			} else {
				border.setBorderSize(size - amount, (long) (amount / bps), null);
				waitForBorder(amount / bps);
			}

		finishHook.run();
		remove();
	}

	public void remove() {
		decreases.remove(this);
	}

	public double getBps() {
		return bps;
	}

	public void setStartHook(Runnable startHook) {
		this.startHook = startHook;
	}

	public void setFinishHook(Runnable finishHook) {
		this.finishHook = finishHook;
	}
}