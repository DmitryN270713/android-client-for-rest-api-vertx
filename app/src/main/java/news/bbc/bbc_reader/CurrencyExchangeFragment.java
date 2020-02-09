package news.bbc.bbc_reader;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import workinginfoclasses.ExchangeUnit;
import workinginfoclasses.ParcelableRate;

import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class CurrencyExchangeFragment extends Fragment
{
    private Spinner spinnerFrom;
    private EditText sumFrom;
    private Spinner spinnerTo;
    private TextView sumTo;
    private boolean isUploaded = false;
    private onUploadingRequest request;
    private boolean receiverRegistered = false;
    private CurrencyReceiver receiver;
    private ArrayList<ExchangeUnit> lsUnit;
    private Map<String, String> shortLongMap;
    private float rateFrom;
    private float rateTo;

    public static final String CURRENCY_SENT = "CURRENCY_RECEIVED";
    public static final String CURRENCY_SENT_KEY = "CURRENCY_SENT_KEY";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = null;
        this.request = (onUploadingRequest) this.getActivity();
        if(!this.receiverRegistered)
        {
            this.receiver = new CurrencyReceiver();
            IntentFilter filter = new IntentFilter(CURRENCY_SENT);
            this.getActivity().registerReceiver(this.receiver, filter);
            this.receiverRegistered = true;
        }
        this.lsUnit = new ArrayList<ExchangeUnit>();
        this.shortLongMap = new HashMap<String, String>();

        view = inflater.inflate(R.layout.currency_exchange_layout, null);
        this.spinnerFrom = (Spinner)view.findViewById(R.id.fromSpinner);
        this.sumFrom = (EditText)view.findViewById(R.id.fromText);
        this.spinnerTo = (Spinner)view.findViewById(R.id.toSpinner);
        this.sumTo = (TextView)view.findViewById(R.id.toText);

        this.ConnectTriggers();
        this.populateShortLongMap();

        return view;
    }

    private void ConnectTriggers()
    {
        this.spinnerFrom.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3)
            {
                rateFrom = 1.0f / lsUnit.get(position).getNewCourse();
                if(!sumFrom.getText().toString().equals("") && !sumFrom.getText().toString().equals("."))
                {
                    calculateSum(Float.valueOf(sumFrom.getText().toString()));
                }
                CurrencyExchangeFragment.this.HideVirtualKeyBoard();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });

        this.spinnerTo.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3)
            {
                rateTo = 1.0f / lsUnit.get(position).getNewCourse();
                if(!sumFrom.getText().toString().equals("") && !sumFrom.getText().toString().equals("."))
                {
                    calculateSum(Float.valueOf(sumFrom.getText().toString()));
                }
                CurrencyExchangeFragment.this.HideVirtualKeyBoard();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });

        this.sumFrom.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable arg0)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3)
            {

            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3)
            {
                if(!sumFrom.getText().toString().equals("") && !sumFrom.getText().toString().equals("."))
                {
                    calculateSum(Float.parseFloat(sumFrom.getText().toString()));
                }
            }});

        this.sumFrom.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                if(!CurrencyExchangeFragment.this.isUploaded)
                {
                    CurrencyExchangeFragment.this.request.onUpload();
                }
            }
        });
    }

    private class CurrencyReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(action.equals(CURRENCY_SENT))
            {
                ArrayList<ParcelableRate> ls = intent.getParcelableArrayListExtra(CURRENCY_SENT_KEY);
                for(int i = 0; i < ls.size(); i++)
                {
                    ExchangeUnit unit = new ExchangeUnit(ls.get(i).getCurrency(),
                            CurrencyExchangeFragment.this.shortLongMap.get(ls.get(i).getCurrency()),
                            ls.get(i).getRate());

                    CurrencyExchangeFragment.this.lsUnit.add(unit);
                }
                CurrencyExchangeFragment.this.lsUnit.add(new ExchangeUnit("EUR", "Euro", 1.0f));


                ExchangeArrayAdapter adapterFrom = new ExchangeArrayAdapter(CurrencyExchangeFragment.this.getActivity(), R.layout.spinner_row, lsUnit);
                ExchangeArrayAdapter adapterTo = new ExchangeArrayAdapter(CurrencyExchangeFragment.this.getActivity(), R.layout.spinner_row, lsUnit);
                CurrencyExchangeFragment.this.spinnerFrom.setAdapter(adapterFrom);
                CurrencyExchangeFragment.this.spinnerTo.setAdapter(adapterTo);

                CurrencyExchangeFragment.this.isUploaded = true;
            }
        }

    }

    private void calculateSum(final float sum)
    {
        DecimalFormat dformat = new DecimalFormat("#.##");
        float result = (sum * this.rateFrom / this.rateTo);
        result = Float.valueOf(dformat.format(result));
        this.sumTo.setText(Float.toString(result));
    }

    private void populateShortLongMap()
    {
        String[] shortNames = { "HUF", "IDR", "ISK", "JPY", "KRW", "LTL", "CZK",
                "LVL", "MTL", "MYR", "NOK", "NZD", "PHP",
                "PLN", "RON", "HRK", "RUB", "SEK", "SGD", "SIT",
                "SKK", "THB", "TRY", "ZAR", "EUR", "USD", "BGN", "DKK",
                "GBP", "CHF", "AUD", "BRL", "CAD", "CNY", "HKD", "ILS",
                "INR", "MXN"};
        String[] longNames = { "Hungarian Forint", "Indonesian Rupiahs", "Icelandic Kronur",
                "Japanese Yen", "South Korean Won", "Lithuanian Litai", "Czech Koruna", "Latvian Lati",
                "Malta Liri", "Malaysian Ringgits", "Norwegian Krone", "New Zealand Dollars",
                "Philippine Pesos", "Polish Zlotych", "Romanian New Lei", "Croatian Kuna", "Russian Rubles",
                "Swedish Kronor", "Singapore Dollars", "Slovenian Tolars", "Slovakian Koruny",
                "Thai Baht", "Turkish New Lira", "South African Rand", "Euro", "U.S.Dollar",
                "Bulgarian lev", "Danish Krone", "British Pound", "Swiss Franc", "Croatian Kuna",
                "Australian Dollar", "Brazilian Real", "Canadian Dollar", "Chinese Yuan", "Hong Kong Dollar",
                "Israeli Shekel", "Indian Rupee", "Mexican peso"};

        for(int i = 0; i < shortNames.length; i++)
        {
            this.shortLongMap.put(shortNames[i], longNames[i]);
        }
    }

    private class ExchangeArrayAdapter extends ArrayAdapter<ExchangeUnit>
    {
        private List<ExchangeUnit> units;

        public ExchangeArrayAdapter(Context context, int textViewResourceId,
                                    List<ExchangeUnit> units)
        {
            super(context, textViewResourceId, units);
            this.units = units;
        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent)
        {
            return constructView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            return constructViewSingle(position, convertView, parent);
        }

        public View constructView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) CurrencyExchangeFragment.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.spinner_row, parent, false);
            TextView tvValue = (TextView)view.findViewById(R.id.fromTV);
            tvValue.setText(this.units.get(position).getShortName() +
                    " / " + this.units.get(position).getLongName());
            return view;
        }

        public View constructViewSingle(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) CurrencyExchangeFragment.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.spinner_row, parent, false);
            TextView tvValue = (TextView)view.findViewById(R.id.fromTV);
            tvValue.setText(this.units.get(position).getShortName());
            return view;
        }
    }

    public interface onUploadingRequest
    {
        public void onUpload();
    }

    private void HideVirtualKeyBoard()
    {
        InputMethodManager manager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(this.sumFrom.getWindowToken(), 0);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        this.isUploaded = false;
    }

    @Override
    public void onDestroy()
    {
        Log.d("ASYNC", "OnCurrencyFragmentDestroyed");
        this.getActivity().unregisterReceiver(this.receiver);
        super.onDestroy();
    }
}
