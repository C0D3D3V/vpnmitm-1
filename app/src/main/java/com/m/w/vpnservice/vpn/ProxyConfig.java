package com.m.w.vpnservice.vpn;


import android.content.Context;

import com.m.w.vpnservice.vpn.builder.BlockingInfoBuilder;
import com.m.w.vpnservice.vpn.builder.DefaultBlockingInfoBuilder;
import com.m.w.vpnservice.vpn.filter.DomainFilter;
import com.m.w.vpnservice.vpn.tcpip.CommonMethods;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by zengzheying on 15/12/28.
 */
public class ProxyConfig {

	public static final ProxyConfig Instance = new ProxyConfig();

	public final static int FAKE_NETWORK_MASK = CommonMethods.ipStringToInt("255.255.0.0");
	public final static int FAKE_NETWORK_IP = CommonMethods.ipStringToInt("10.231.0.0");

	int mDnsTtl;

	String mSessionName;
	int mMtu;
	DomainFilter mDomainFilter;
	BlockingInfoBuilder mBlockingInfoBuilder;
	private VpnStatusListener mVpnStatusListener;


	public ProxyConfig() {

	}

	public static boolean isFakeIP(int ip) {
		return (ip & ProxyConfig.FAKE_NETWORK_MASK) == ProxyConfig.FAKE_NETWORK_IP;
	}

	public void setDomainFilter(DomainFilter domainFilter) {
		mDomainFilter = domainFilter;
	}

	public void prepare() throws IllegalStateException {
		if (mDomainFilter != null) {
			mDomainFilter.prepare();
		} else {
			throw new IllegalStateException("ProxyConfig need an instance of DomainFilter," +
					" you should set an DomainFilter object by call the setDomainFilter() method");
		}
	}

	public IPAddress getDefaultLocalIP() {
		return new IPAddress("10.8.0.2", 32);
	}

	public int getDnsTTL() {
		if (mDnsTtl < 30) {
			mDnsTtl = 30;
		}
		return mDnsTtl;
	}


	public int getMTU() {
		if (mMtu > 1400 && mMtu <= 20000) {
			return mMtu;
		} else {
			return 20000;
		}
	}

	public boolean needProxy(String host, int ip) {
		boolean filter = mDomainFilter != null && mDomainFilter.needFilter(host, ip);
		return filter || isFakeIP(ip);
	}

	public void setBlockingInfoBuilder(BlockingInfoBuilder blockingInfoBuilder) {
		mBlockingInfoBuilder = blockingInfoBuilder;
	}

	public ByteBuffer getBlockingInfo() {
		if (mBlockingInfoBuilder != null) {
			return mBlockingInfoBuilder.getBlockingInformation();
		} else {
			return DefaultBlockingInfoBuilder.get().getBlockingInformation();
		}
	}

	public void setVpnStatusListener(VpnStatusListener vpnStatusListener) {
		mVpnStatusListener = vpnStatusListener;
	}

	public void onVpnStart(Context context) {
		if (mVpnStatusListener != null) {
			mVpnStatusListener.onVpnStart(context);
		}
	}


	public void onVpnEnd(Context context) {
		if (mVpnStatusListener != null) {
			mVpnStatusListener.onVpnEnd(context);
		}
	}

	public interface VpnStatusListener {
		void onVpnStart(Context context);

		void onVpnEnd(Context context);
	}

	public static class IPAddress {
		public final String Address;
		public final int PrefixLength;

		public IPAddress(String address, int prefixLength) {
			Address = address;
			PrefixLength = prefixLength;
		}

		public IPAddress(String ipAddressString) {
			String[] arrStrings = ipAddressString.split("/");
			String address = arrStrings[0];
			int prefixLength = 32;
			if (arrStrings.length > 1) {
				prefixLength = Integer.parseInt(arrStrings[1]);
			}

			this.Address = address;
			this.PrefixLength = prefixLength;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof IPAddress)) {
				return false;
			} else {
				return this.toString().equals(o.toString());
			}
		}

		@Override
		public String toString() {
			return String.format("%s/%d", Address, PrefixLength);
		}
	}
}
