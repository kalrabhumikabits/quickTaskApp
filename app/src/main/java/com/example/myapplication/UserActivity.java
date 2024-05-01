package com.example.myapplication;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class UserActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;



    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_user );
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.showOverflowMenu();
        listView=findViewById(R.id.listView);
        //Read task
        ParseQuery<ParseObject> query=new ParseQuery<ParseObject> ( "Note" );
        query.whereEqualTo ( "username",ParseUser.getCurrentUser().getUsername() );
        query.orderByDescending ( "updatedAt");
        query.findInBackground ( new FindCallback<ParseObject> ( ) {
            @Override
            public void done ( List<ParseObject> objects, ParseException e ) {
                arrayList = new ArrayList<>( );
                for(ParseObject object:objects){
                    arrayList.add(object.get("note").toString());
                }
                adapter = new ArrayAdapter<String> ( UserActivity.this, android.R.layout.simple_list_item_1,arrayList );
                listView.setAdapter ( adapter );
            }
        } );

        //update task
        listView.setOnItemClickListener ( new AdapterView.OnItemClickListener ( ) {
            @Override
            public void onItemClick ( AdapterView<?> parent, View view, int position, long id ) {
                ParseQuery<ParseObject> query=new ParseQuery<ParseObject> ( "Note" );
                query.whereEqualTo ("username",ParseUser.getCurrentUser ().getUsername ());
                query.whereEqualTo ( "note",arrayList.get(position) );
                query.setLimit(1);
                query.findInBackground ( new FindCallback<ParseObject> ( ) {
                    @Override
                    public void done ( List<ParseObject> objects, ParseException e ) {
                        String objectID="";
                        for(ParseObject object:objects){
                            objectID=object.getObjectId ();
                        }
                        ParseQuery<ParseObject> query= new ParseQuery<ParseObject> ( "Note" );
                        query.getInBackground ( objectID, new GetCallback<ParseObject> ( ) {
                            @Override
                            public void done ( ParseObject object, ParseException e ) {
                                if(e==null){
                                    AlertDialog.Builder builder=new AlertDialog.Builder ( UserActivity.this );
                                    final EditText noteEditText =new EditText(UserActivity.this);
                                    noteEditText.setText(object.get("note").toString ());
                                    builder.setTitle ( "Update now" )
                                            .setView ( noteEditText )
                                            .setPositiveButton ( "save", new DialogInterface.OnClickListener ( ) {
                                        @Override
                                        public void onClick ( DialogInterface dialog, int which ) {
                                            object.put("note",noteEditText.getText ().toString ());
                                            object.saveInBackground ( new SaveCallback ( ) {
                                                @Override
                                                public void done ( ParseException e ) {
                                                    if(e==null){
                                                        Toast.makeText ( UserActivity.this ,"Note updated Succesfully",Toast.LENGTH_SHORT).show();
                                                        refreshActivity ();
                                                    }else{
                                                        Toast.makeText ( UserActivity.this,"Try again later",Toast.LENGTH_SHORT ).show();
                                                    }
                                                }
                                            } );
                                        }
                                    } )
                                            .setNegativeButton ( "cancel", new DialogInterface.OnClickListener ( ) {
                                                @Override
                                                public void onClick ( DialogInterface dialog, int which ) {
                                                    dialog.cancel();
                                                }
                                            } )
                                            .show();
                                }else{
                                    Toast.makeText ( UserActivity.this, e.getMessage (),Toast.LENGTH_SHORT).show();
                                }
                            }
                        } );
                    }
                } );
            }
        } );
        //delete task
        listView.setOnItemLongClickListener ( new AdapterView.OnItemLongClickListener ( ) {
            @Override
            public boolean onItemLongClick ( AdapterView<?> parent, View view, int position, long id ) {
                ParseQuery<ParseObject> query = new ParseQuery<ParseObject> ("Note");
                query.whereEqualTo ( "username",ParseUser.getCurrentUser().getUsername() );
                query.whereEqualTo ( "note",arrayList.get(position) );
                query.setLimit ( 1 );
                query.findInBackground ( new FindCallback<ParseObject> ( ) {
                    @Override
                    public void done ( List<ParseObject> objects, ParseException e ) {
                        String objectID="";
                        for(ParseObject object:objects){
                            objectID=object.getObjectId ();
                        }
                        ParseQuery<ParseObject> query= new ParseQuery<ParseObject> ( "Note" );
                        query.getInBackground ( objectID, new GetCallback<ParseObject> ( ) {
                            @Override
                            public void done ( ParseObject object, ParseException e ) {
                                if(e==null){
                                    AlertDialog.Builder builder=new AlertDialog.Builder ( UserActivity.this );
                                    builder.setTitle ( "Delete this task" )
                                            .setMessage ( "Confirm deleting this task" )
                                            .setPositiveButton ( "Yes", new DialogInterface.OnClickListener ( ) {
                                                @Override
                                                public void onClick ( DialogInterface dialog, int which ) {
                                                    object.deleteInBackground ( new DeleteCallback ( ) {
                                                        @Override
                                                        public void done ( ParseException e ) {
                                                            if(e==null){
                                                                Toast.makeText ( UserActivity.this,"Task is deleted",Toast.LENGTH_SHORT ).show();
                                                                refreshActivity ();
                                                            }else{
                                                                Toast.makeText ( UserActivity.this,"Try again later",Toast.LENGTH_SHORT ).show();
                                                            }
                                                        }
                                                    } );
                                                }
                                            } )
                                            .setNegativeButton ( "No", new DialogInterface.OnClickListener ( ) {
                                                @Override
                                                public void onClick ( DialogInterface dialog, int which ) {
                                                    dialog.cancel();
                                                }
                                            } )
                                            .show();
                                }
                                else{
                                    Toast.makeText ( UserActivity.this,e.getMessage(),Toast.LENGTH_SHORT ).show();
                                }
                            }
                        } );
                    }
                } );
                return true;
            }
        } );
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu ) {
        getMenuInflater ().inflate ( R.menu.menu, menu );
        return true;
    }

    //task create
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.logout_menu){
            ParseUser.logOut();
            final Intent intent = new Intent(UserActivity.this,MainActivity.class);
            startActivity(intent);
        }else if(id==R.id.add_item_menu){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText noteEditText = new EditText(this);
            builder.setTitle("Add a new task")
                    .setView(noteEditText)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ParseObject note = new ParseObject("Note");
                            note.put("username",ParseUser.getCurrentUser().getUsername());
                            note.put("note",noteEditText.getText().toString());
                            note.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e==null){
                                        Toast.makeText(UserActivity.this, "Note saved successfully!", Toast.LENGTH_SHORT).show();
//                                        Intent intent1= new Intent ( UserActivity.this,UserActivity.class );
//                                        finish();
//                                        startActivity (intent1);
                                        refreshActivity();
                                    }else {
                                        Toast.makeText(UserActivity.this, "Oops - Try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
    private void refreshActivity(){
        Intent intent1 = new Intent(UserActivity.this,UserActivity.class);
        finish();
        startActivity(intent1);
    }
}