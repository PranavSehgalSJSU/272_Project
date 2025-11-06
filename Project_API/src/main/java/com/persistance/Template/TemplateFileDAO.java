package com.persistance.Template;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : TemplateFileDAO.java
//  AUTHOR : Pranav Sehgal <PranavSehgalCS>
//  DESCRIPTION: Is a template FileDAO.java file
//               IMPLEMENTS TemplateDAO interface
//               DEFINES functions declared in the FileDAO.java file with @override
//               DECLARE functions and variables not in FileDAO.java as private
//               DECLARE functions and variables not in FileDAO.java as public static if they need to be
//                      accessed elsewhere.
//               VALUES are taken from the src/main/resources/application.properties file
//
///////////////////////////////////////////////////////////////////////////////////////////////////////
import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import com.model.Template;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
///////////////////////////////////////////////////////////////////////////////////////////////////////

@Component
public class TemplateFileDAO implements TemplateDAO{
    public int nextID;
    private String fileName;
    private ObjectMapper fileHandler;
    public Boolean initialized = false;
    public Map<Integer, Template> TemplateHolder = new TreeMap<Integer, Template>();

    public boolean testingCatch = false;

    public TemplateFileDAO( ObjectMapper fileHandler,
                            @Value("${template.data}") String fileName){
        try {
            if(fileHandler == null){
                throw new NullPointerException();
            }
            this.fileName = fileName;
            this.fileHandler = fileHandler;
            this.initialized = this.loadTemplates();
        } catch (Exception e) {
            System.out.println("ERROR at constructor while loading initial file --> " + e);
        }
    }
    
    public Boolean loadTemplates(){
        try {
            exceptionTester();
            this.TemplateHolder.clear();
            Template[] templateArray = fileHandler.readValue(new File(fileName),Template[].class);
            for(Template i:templateArray){
                this.TemplateHolder.put(i.getTemid(), i);
                if(i.getTemid()>=this.nextID){
                    this.nextID = (i.getTemid() + 1); 
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("ERROR While loading templates from file --> " + e);
        }
        return false;
    }
    public Boolean saveTemplates(){
        try {
            exceptionTester();
            fileHandler.writeValue(new File(fileName), this.TemplateHolder.values());
            return true;
        } catch (Exception e) {
            System.out.println("ERROR While saving to file : --> "+ e);
        }
        return false;
    }
    public void exceptionTester() throws Exception{
        if(testingCatch){
            throw new RuntimeException();
        }
    } 

    @Override
    public Template[] getTemplates(int temid){
        try {
            exceptionTester();
            if(!this.initialized){
                this.initialized = loadTemplates();
            }
            Template[] retVal =  new Template[1];
            if(temid != -1){
                for(Template i: this.TemplateHolder.values()){
                    if(i.getTemid() == temid){
                        retVal[0]=i;
                        return retVal;
                    }
                }
                return retVal;
            } else{
                retVal =  new Template[this.TemplateHolder.size()];
                this.TemplateHolder.values().toArray(retVal);
                return retVal;
            }
        } catch (Exception e) {
            System.out.println("Error At Function While Getting With Id (" + temid + ") -->" + e);
            return null;
        }
    }
    
    @Override
    public Boolean createTemplate(String tname, String tmess, Boolean tbool){
        try {
            exceptionTester();
            this.nextID++;
            Template newTemplate = new Template((nextID-1), tname, tmess, tbool);
            this.TemplateHolder.put(newTemplate.getTemid(), newTemplate);
            return saveTemplates();
        }catch (Exception e) {
            System.out.println("ERROR at function while creating template --> " + e);
        }
        return false;
    }

    @Override
    public Boolean updateTemplate(int temid, String tname, String tmess, Boolean tbool){
        try{
            exceptionTester();
            Boolean existState = false;
            for(Template i:TemplateHolder.values()){
                if(i.getTemid() == temid){
                    existState= true;
                    break;
                }
            }
            if(existState){
                this.TemplateHolder.put(temid, new Template(temid, tname, tmess, tbool));
            }else{
                return false;
            }
            return saveTemplates();
        }catch (Exception e) {
            System.out.println("ERROR at function while updating template --> " + e);
        }
        return false;
    }

    @Override
    public Boolean deleteTemplate(int temid){
        try {
            exceptionTester();
            Template remVal = TemplateHolder.remove(temid);
            if(remVal == null){
                return false;
            }
            return saveTemplates();
        } catch (Exception e) {
            System.out.println("ERROR at function while deleting template --> " + e);
        }
        return false;
    }
    
}
