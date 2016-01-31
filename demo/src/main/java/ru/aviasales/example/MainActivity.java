package ru.aviasales.example;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import ru.aviasales.core.AviasalesSDK;
import ru.aviasales.template.ui.fragment.AviasalesFragment;

public class MainActivity extends ActionBarActivity {

        private AviasalesFragment aviasalesFragment;

	@Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);


		AviasalesSDK.getInstance().init(this); // initialization of AviasalesSDK
                setContentView(R.layout.activity_main);

                initFragment();
	}

	private void init(Bundle savedInstanceState) {
		initFragment();
	}

	private void initFragment() {
        FragmentManager fm = getSupportFragmentManager();

        aviasalesFragment = (AviasalesFragment) fm.findFragmentByTag(AviasalesFragment.TAG); // finding fragment by tag

		if (aviasalesFragment == null) { 
                aviasalesFragment = (AviasalesFragment) AviasalesFragment.newInstance();
		}

		FragmentTransaction fragmentTransaction = fm.beginTransaction(); // adding fragment to fragment manager
                fragmentTransaction.replace(R.id.fragment_place, aviasalesFragment, AviasalesFragment.TAG);
                fragmentTransaction.commit();
	}

	@Override
	 public void onBackPressed() {
		if (!aviasalesFragment.onBackPressed()) {
                super.onBackPressed();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
