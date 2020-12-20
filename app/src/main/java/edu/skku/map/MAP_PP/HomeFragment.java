package edu.skku.map.MAP_PP;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    EditText input;
    TextView translate;
    EditText output;
    TextView save;
    TextView edit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.PhraseBook_header).setVisibility(View.GONE);
        getActivity().findViewById(R.id.papago_header).setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        input = (EditText)view.findViewById(R.id.input);
        translate = (TextView)view.findViewById(R.id.translate);
        output = (EditText)view.findViewById(R.id.output);
        save = (TextView)view.findViewById(R.id.save);
        edit = (TextView)view.findViewById(R.id.edit);

        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BackgroundTask().execute();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                output.setFocusable(true);
                output.setFocusableInTouchMode(true);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    // No session user
                    Toast.makeText(getContext(), "Please log in to save data", Toast.LENGTH_SHORT).show();
                }
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                Date now = new Date();
                formatter.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String date = formatter.format(now);
                String input_save = input.getText().toString();
                String output_save = output.getText().toString();
                String userId = user.getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference userdata = database.getReference("Phrasebooks").child(userId).child("Studying");
                listdata listData = new listdata(date, input_save, output_save, "soso");
                userdata.push().setValue(listData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getContext(), "successfully added to phrase book", Toast.LENGTH_SHORT).show();
                    }
                });;
                input.setText("");
                output.setText("");
            }
        });
        return view;
    }

    class BackgroundTask extends AsyncTask<Integer, Integer, Integer> {

        String input_str = input.getText().toString();
        String result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {
            String clientId = "_kZG2f_eZd3mIkVJBT9M";//애플리케이션 클라이언트 아이디값";
            String clientSecret = "JIRbjfAJbf";//애플리케이션 클라이언트 시크릿값";

            String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
            String text;
            try {
                text = URLEncoder.encode(input_str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("인코딩 실패", e);
            }

            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("X-Naver-Client-Id", clientId);
            requestHeaders.put("X-Naver-Client-Secret", clientSecret);

            String responseBody = post(apiURL, requestHeaders, text);

            System.out.println(responseBody);
            return 0;
        }

        private String post(String apiUrl, Map<String, String> requestHeaders, String text){
            HttpURLConnection con = connect(apiUrl);
            String postParams = "source=en&target=ko&text=" + text; //원본언어: 목적언어: 영어 (en) -> 한국어 (ko)
            try {
                con.setRequestMethod("POST");
                for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }

                con.setDoOutput(true);
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    wr.write(postParams.getBytes());
                    wr.flush();
                }

                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 응답
                    return readBody(con.getInputStream());
                } else {  // 에러 응답
                    return readBody(con.getErrorStream());
                }
            } catch (IOException e) {
                throw new RuntimeException("API 요청과 응답 실패", e);
            } finally {
                con.disconnect();
            }
        }

        private HttpURLConnection connect(String apiUrl){
            try {
                URL url = new URL(apiUrl);
                return (HttpURLConnection)url.openConnection();
            } catch (MalformedURLException e) {
                throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
            } catch (IOException e) {
                throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
            }
        }

        private String readBody(InputStream body){
            InputStreamReader streamReader = new InputStreamReader(body);

            try (BufferedReader lineReader = new BufferedReader(streamReader)) {
                StringBuilder responseBody = new StringBuilder();

                String line;
                while ((line = lineReader.readLine()) != null) {
                    responseBody.append(line);
                }
                result = responseBody.toString();
                return responseBody.toString();
            } catch (IOException e) {
                throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            if(element.getAsJsonObject().get("errorMessage") != null){
                //error
            }
            else if(element.getAsJsonObject().get("message") != null){
                output.setText(element.getAsJsonObject().get("message").getAsJsonObject().get("result").getAsJsonObject().get("translatedText").getAsString());
            }
        }
    }

    public class listdata {
        public String date;
        public String input;
        public String output;
        public String level;

        public listdata(){};

        public listdata(String date, String input, String output, String level) {
            this.date = date;
            this.input = input;
            this.output = output;
            this.level = level;
        }
    }
}


