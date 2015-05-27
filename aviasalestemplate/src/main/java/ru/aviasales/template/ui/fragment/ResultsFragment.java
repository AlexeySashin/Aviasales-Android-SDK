package ru.aviasales.template.ui.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ru.aviasales.core.AviasalesSDK;
import ru.aviasales.core.search.object.SearchData;
import ru.aviasales.core.search.object.TicketData;
import ru.aviasales.core.search.params.SearchParams;
import ru.aviasales.template.R;
import ru.aviasales.template.currencies.Currency;
import ru.aviasales.template.filters.manager.FiltersManager;
import ru.aviasales.template.ticket.TicketManager;
import ru.aviasales.template.ui.adapter.ResultsRecycleViewAdapter;
import ru.aviasales.template.ui.dialog.CurrencyFragmentDialog;
import ru.aviasales.template.ui.dialog.ResultsSortingDialog;
import ru.aviasales.template.ui.view.ActionBarTitleFlightView;
import ru.aviasales.template.ui.view.EmptyView;
import ru.aviasales.template.utils.CurrencyUtils;
import ru.aviasales.template.utils.StringUtils;

public class ResultsFragment extends BaseFragment {

	public static int sortingType = ResultsSortingDialog.SORTING_BY_PRICE;
	private static int resultsCount = -1;

	private ResultsRecycleViewAdapter resultsAdapter;

	private View rootView;
	private RecyclerView resultsListView;
	private EmptyView emptyView;
	private TextView currencyTextView;

	public static ResultsFragment newInstance() {
		return new ResultsFragment();
	}

	@Override
	protected void resumeDialog(String removedDialogFragmentTag) {
		if (removedDialogFragmentTag.equals(ResultsSortingDialog.TAG)) {
			createSortingDialog();
		}

		if (removedDialogFragmentTag.equals(CurrencyFragmentDialog.TAG)) {
			createCurrencyDialog();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.results_fragment, container, false);
		setUpViews();
		setupActionBarCustomView();
		return rootView;
	}

	@Override
	public void onDestroyView() {
		resultsAdapter = null;
		super.onDestroyView();
	}

	private void setupActionBarCustomView() {
		ActionBarTitleFlightView titleView = new ActionBarTitleFlightView(getActivity());
		if (getSearchParams() != null) {
			titleView.setData(getSearchParams().getOriginIata(), getSearchParams().getDestinationIata());
		}
		getActionBar().show();
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(titleView);
	}

	private void setUpViews() {

		emptyView = (EmptyView) rootView.findViewById(R.id.empty_view);
		emptyView.setType(EmptyView.TYPE_RESULTS);
		emptyView.setOnButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				clearFilters();
			}
		});
		emptyView.setVisibility(View.GONE);

		resultsListView = (RecyclerView) rootView.findViewById(R.id.lv_results);
		setUpListView(resultsListView);
		resultsListView.setHasFixedSize(true);

		resultsListView.setLayoutManager(new LinearLayoutManager(getActivity()));

		setupEmptyViewVisibility();
	}

	private void setupEmptyViewVisibility() {
		if (resultsAdapter == null ||
				resultsListView == null) return;

		if (resultsAdapter.getItemCount() == 0) {
			resultsListView.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
		} else {
			resultsListView.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
		}
	}

	private void setUpListView(RecyclerView listView) {

		final ResultsRecycleViewAdapter adapter = createOrRefreshAdapter();

		listView.setAdapter(adapter);

		adapter.setListener(new ResultsRecycleViewAdapter.OnClickListener() {
			@Override
			public void onClick(final TicketData ticketData, int position) {
				if (getActivity() == null) return;

				showDetails(ticketData);
			}
		});
		adapter.notifyDataSet();
	}

	private ResultsRecycleViewAdapter createOrRefreshAdapter() {
		ResultsRecycleViewAdapter adapter;

		if (resultsAdapter == null) {
			resultsAdapter = new ResultsRecycleViewAdapter(getActivity());
		} else {
			resultsAdapter.reloadFilteredTickets(FiltersManager.getInstance().getFilteredTickets());
		}
		adapter = resultsAdapter;

		return adapter;
	}

	private SearchData getSearchResults() {
		return AviasalesSDK.getInstance().getSearchData();
	}

	private void showDetails(TicketData ticketData) {
		TicketManager.getInstance().init(ticketData, AviasalesSDK.getInstance().getSearchData().getGatesInfo(), AviasalesSDK.getInstance().getSearchParamsOfLastSearch());
		startFragment(TicketDetailsFragment.newInstance(), true);
	}

	private void checkAppDataAvailability() {
		if (getActivity() == null || getSearchResults() == null || getSearchParams() == null) {
			Toast.makeText(getActivity(), getResources().getString(R.string.toast_no_search_results), Toast.LENGTH_SHORT).show();
			getActivity().onBackPressed();
		} else {
			if (resultsAdapter != null) {
				resultsAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.results_menu, menu);
		View currency = null;

		if (Build.VERSION.SDK_INT <= 10) {
			currency = MenuItemCompat.getActionView(menu.findItem(R.id.currency));
		} else {
			currency = menu.findItem(R.id.currency).getActionView();
		}

		currencyTextView = (TextView) currency.findViewById(R.id.tv_currency);

		updateCurrencyTextView();

		currency.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createCurrencyDialog();
			}
		});

		super.onCreateOptionsMenu(menu, inflater);
	}

	private void updateCurrencyTextView() {
		currencyTextView.setText(StringUtils.getSpannableString(CurrencyUtils.getAppCurrency(getActivity()), new UnderlineSpan()));
	}

	private void createCurrencyDialog() {

		CurrencyFragmentDialog dialog = CurrencyFragmentDialog.newInstance(new CurrencyFragmentDialog.OnCurrencyChangedListener() {
			@Override
			public void onCurrencyChanged(String code) {
				CurrencyUtils.setAppCurrency(code, getActivity());
				updateCurrencyTextView();
				resultsAdapter.notifyDataSetChanged();
				dismissDialog();
			}

			@Override
			public void onCancel() {
				dismissDialog();
			}
		});
		List<Currency> currencies = CurrencyUtils.getCurrenciesList();
		dialog.setItems(currencies);
		createDialog(dialog);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.sort) {
			if (resultsAdapter != null && resultsListView != null) {
				createSortingDialog();
			}
			return true;
		} else if (id == R.id.filters) {
			startFragment(FiltersFragment.newInstance(), true);
			return true;
		} else if (id == R.id.currency) {
			startFragment(FiltersFragment.newInstance(), true);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void createSortingDialog() {
		createDialog(ResultsSortingDialog.newInstance(sortingType, new ResultsSortingDialog.OnSortingChangedListener() {
			@Override
			public void onSortingChanged(int sortingType) {
				ResultsFragment.sortingType = sortingType;
				resultsListView.setAdapter(resultsAdapter);
				resultsAdapter.notifyDataSet();
				dismissDialog();
			}

			@Override
			public void onCancel() {
				dismissDialog();
			}
		}));
	}

	@Override
	public void onResume() {
		super.onResume();
		checkAppDataAvailability();

		if (resultsCount != -1 && resultsCount != resultsAdapter.getItemCount()) {
			resultsListView.scrollToPosition(0);
			resultsCount = -1;
		}

	}

	protected SearchParams getSearchParams() {
		return AviasalesSDK.getInstance().getSearchParamsOfLastSearch();
	}

	public void updateResults() {
		if (resultsAdapter != null) {
			resultsAdapter.reloadFilteredTickets(FiltersManager.getInstance().getFilteredTickets());
		}

		setupEmptyViewVisibility();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();

		if (resultsAdapter != null) {
			resultsCount = resultsAdapter.getItemCount();
		}
	}

	private void clearFilters() {
		FiltersManager.getInstance().getFilters().clearFilters();
		FiltersManager.getInstance().filterSearchData(AviasalesSDK.getInstance().getSearchData(), new FiltersManager.OnFilterResultListener() {
			@Override
			public void onFilteringFinished(List<TicketData> filteredTicketsData) {
				updateResults();
			}
		});
	}

}
