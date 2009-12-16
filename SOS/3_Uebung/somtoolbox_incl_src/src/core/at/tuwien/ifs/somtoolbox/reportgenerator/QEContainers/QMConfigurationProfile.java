package at.tuwien.ifs.somtoolbox.reportgenerator.QEContainers;


import java.util.ArrayList;
import java.util.Arrays;

public class QMConfigurationProfile {

    private ArrayList[] QMCriteriaContainer ;
    
    public int SIZE;
    
    public QMConfigurationProfile(int size){      
        this.QMCriteriaContainer = new ArrayList[size];
        Arrays.fill(QMCriteriaContainer, null);
        this.SIZE=size;
    }
    
    public void createNewElement(int index){
        this.QMCriteriaContainer[index] = new ArrayList();
    }
    
    public void insert(int index, Object o){
        this.QMCriteriaContainer[index].add(o);
    }
    
    
    public void insert (int index, int pos, Object o){
        this.QMCriteriaContainer[index].add(pos,o);
    }
    
    public boolean isEmpty(){
        boolean empty = true;
        for (ArrayList element : this.QMCriteriaContainer) {
            if (element != null) {
                empty = false;
            }
        }
        return empty;
    }
    
    public boolean isNullatPos(int index){
        if(this.QMCriteriaContainer[index] == null)
            return true;
        else
            return false;
    }
    
    public int lengthOfElement(int index){
        return this.QMCriteriaContainer[index].size();
    }
    
    public Object getElement(int index, int pos){
        return this.QMCriteriaContainer[index].get(pos);
    }
}
