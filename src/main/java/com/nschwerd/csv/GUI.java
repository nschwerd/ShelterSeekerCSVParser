package com.nschwerd.csv;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GUI extends JFrame{
    private static final long serialVersionUID = -4530296704743158611L;
    
    public static void main(String[] args) {
        System.out.println("Starting CSV parser");

        FileInputStream serviceAccount = null;
        try {
            JFileChooser jfcFB = new JFileChooser();
            jfcFB.setDialogTitle("Select Firebase account key");
            jfcFB.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfcFB.setFileFilter(new FileNameExtensionFilter("Firebase json", "json"));
            jfcFB.showOpenDialog(null);
            serviceAccount = new FileInputStream(jfcFB.getSelectedFile());
            FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setDatabaseUrl("https://gatech-shelterseeker.firebaseio.com").build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        new GUI();
    }
    
    public GUI() {
        setBackground(Color.GRAY);
        setSize(new Dimension(600, 400));
        setResizable(false);
        setTitle("CSV to Firebase");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

//      Text and JSP
        log = new JTextArea();
        log.setBackground(Color.WHITE);
        log.setEditable(false);
        JScrollPane jsp = new JScrollPane(log);
     
//      Button and panel
        fileButton = new JButton("Select CSV");
        fileButton.setSize(new Dimension(60, 40));
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                jfc.setDialogTitle("Select shelter CSV");
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setFileFilter(new FileNameExtensionFilter("CSVs", "csv"));
                int res = jfc.showOpenDialog(null);
                if (res == JFileChooser.APPROVE_OPTION) {
                    log.append("Selected file: " + jfc.getSelectedFile().getPath() + "\n");
                    try {
                        readF(jfc.getSelectedFile());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        
        buttonPane = new JPanel();
        buttonPane.setMaximumSize(new Dimension(600, 20));
        buttonPane.setBackground(Color.LIGHT_GRAY);
        buttonPane.add(Box.createVerticalStrut(20));
        buttonPane.add(fileButton);
        
        add(jsp, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    public void readF(File f) throws IOException {
        Iterable <CSVRecord> rec = CSVFormat.DEFAULT.withHeader("Unique Key", "Shelter Name", "Capacity", "Restrictions", "Longitude", "Latitude", "Address", "Special Notes", "Phone Number").parse(new FileReader(f));
        Shelter s = new Shelter();
        
        for (CSVRecord r: rec) {
//           Skip header/ unkeyed shelters
            if (!Pattern.matches("[0-9]+", r.get("Unique Key")))
                continue;
            
            s = new Shelter();
            s.setUID(r.get("Unique Key"));
            s.setName(r.get("Shelter Name"));
            s.setCapacityStr(r.get("Capacity"));
            s.setRestrictions(r.get("Restrictions"));
            s.setLon(Double.parseDouble(r.get("Longitude")));
            s.setLat(Double.parseDouble(r.get("Latitude")));
            s.setAddress(r.get("Address"));
            s.setNotes(r.get("Special Notes"));
            s.setPhone(r.get("Phone Number"));
            s.setVeteran(Pattern.matches(".*[vV]eterans.*", s.getNotes()));
            
//          capacityNum to -1 if complex input provided
            if (Pattern.matches("[0-9]+", s.getCapacityStr()))
                s.setCapacityNum(Integer.parseInt(s.getCapacityStr()));
            else 
                s.setCapacityNum(-1);
            
            if (Pattern.matches(".*[wW]omen.*", s.getRestrictions()))
                s.setGender(Shelter.Gender.FEMALE);
            else if (Pattern.matches(".*[mM]en.*", s.getRestrictions()))
                s.setGender(Shelter.Gender.MALE);
            else
                s.setGender(Shelter.Gender.ALL);
            
            if (Pattern.matches(".*[cC]hildren.*", s.getRestrictions()))
                s.setAgeRest(Shelter.AgeRest.CHILDREN);
            else if (Pattern.matches(".*[nN]ewborns.*", s.getRestrictions())) 
                s.setAgeRest(Shelter.AgeRest.FAMILIESWITHNEWBORNS);
            else if (Pattern.matches(".*[yY]oung\\s?[aA]dults?.*", s.getRestrictions()))
                s.setAgeRest(Shelter.AgeRest.YOUNGADULTS);
            else
                s.setAgeRest(Shelter.AgeRest.ALL);            
            
            Shelter t = getShelter(s.getUID());
            
            if (t == null) {
                log.append("Adding new shelter with id: " + s.getUID() + "\n");
                addShelter(s);
            } else if (!s.equals(t)) {
                log.append("Updating existing shelter with id: " + s.getUID() + "\n");
                addShelter(s);
            } else
                log.append("Identical shelter already exists with id: " + s.getUID() + "\n");
        }
        log.append("~~File import complete~~\n");
    }
    
    private void addShelter(Shelter s) {
        StoreObjectSynchronousTask<Shelter> task = new StoreObjectSynchronousTask<>(DatabaseKey.SHELTER, s.getUID());
        DatabaseException ex = task.run(s);
        if (ex != null)
            throw ex;
    }
    
    private Shelter getShelter(String uid) {
        RetrieveObjectSynchronousTask<Shelter> task = new RetrieveObjectSynchronousTask<>(DatabaseKey.SHELTER, uid, Shelter.class);
        DatabaseException ex = task.run();
        if (ex != null)
            throw ex;
        return task.getValue();
    }
    
    private class RetrieveObjectSynchronousTask<T> {
        private String key;
        private Class<T> type;
        private T value;
        private DatabaseError error;
        
        private RetrieveObjectSynchronousTask(DatabaseKey dbKey, String id, Class<T> type) {
            this.key = "/" + dbKey + "/" + id;
            this.type = type;
        }
        
        private DatabaseException run() {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(key);
            final CountDownLatch latch = new CountDownLatch(1);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    value = dataSnapshot.getValue(type);
                    latch.countDown();
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    error = databaseError;
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (error == null) {
                return null;
            } else {
                return error.toException();
            }
        }
        
        private T getValue() {
            return value;
        }
        
    }
    
    private class StoreObjectSynchronousTask<T> {
        private String key;
        private DatabaseError error;
        
        private StoreObjectSynchronousTask(DatabaseKey dbKey, String id) {
            this.key = "/" + dbKey + "/" + id;
        }
        
        private DatabaseException run(T object) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(key);
            final CountDownLatch latch = new CountDownLatch(1);
            myRef.setValue(object, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        error = databaseError;
                    }
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (error == null) {
                return null;
            } else {
                return error.toException();
            }
        }
        
    }
    
    private enum DatabaseKey {
        USER("SSUser"), SHELTER("SSShelter"), RESERVATION("SSReservation");

        private String key;
        private DatabaseKey(String k) {
            key = k;
        }

        @Override
        public String toString() {
            return key;
        }
    }
    
    private JTextArea log;
    private JPanel buttonPane;
    private JButton fileButton;
}