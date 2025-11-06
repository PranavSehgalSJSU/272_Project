package com.controller;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : TemplateController.java
//  AUTHOR : Pranav Sehgal <PranavSehgalCS>
//
//  DESCRIPTION: Is a template Controller file with preconfigurations all set.
//               USE this to take requests [GET, POST, PUT & DELETE] from specific URLS
//               USE https://www.postman.com/ to test backend if you don't have frontent yet
//               IMPORT for PATCH is included as a comment 
///////////////////////////////////////////////////////////////////////////////////////////////////////
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.model.Template;
import com.persistance.Template.TemplateDAO;
///////////////////////////////////////////////////////////////////////////////////////////////////////
@RestController
@RequestMapping("template")
public class TemplateController{
    private  TemplateDAO templateDao;
    public boolean testingCatch = false;
    private static final Logger LOG = Logger.getLogger(TemplateController.class.getName());
    public TemplateController(TemplateDAO templateDao) {
        this.templateDao = templateDao;
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////
    @GetMapping("")
    public ResponseEntity<Template[]>getAllTemps( ){
        LOG.info("\nGET /template");
        try {
            Template[] retVal = this.templateDao.getTemplates(-1);
            return new ResponseEntity<Template[]>(retVal,HttpStatus.OK);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{temid}")
    public ResponseEntity<Template[]>getTLFromPaths(  @PathVariable Integer temid){
        LOG.info("\nGET /template/"+temid);
        try {
            
            Template[] retVal = this.templateDao.getTemplates(temid);
            if(retVal!=null){
                return new ResponseEntity<Template[]>(retVal ,HttpStatus.OK);
            }else{
                
                return new ResponseEntity<Template[]>(HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/params" )
    public ResponseEntity<Template[]>getTLFromParam(  @RequestParam(name = "temid", required = true) Integer temid){
        LOG.info("\nGET /template/"+temid);
        try {
            
            Template[] retVal = this.templateDao.getTemplates(temid);
            if(retVal!=null){
                return new ResponseEntity<Template[]>(retVal ,HttpStatus.OK);
            }else{
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("")
    public ResponseEntity<Boolean> createTemplate(  @RequestParam(name = "tname", required = true) String tname, 
                                                    @RequestParam(name = "tmess", required = true) String tmess,
                                                    @RequestParam(name = "tbool", required = true) Boolean tbool
                                                ){
        LOG.info("\nPOST /template/" + tname );
        try{
            Boolean responseVal = templateDao.createTemplate(tname,tmess,tbool);
            if(responseVal){
                return new ResponseEntity<Boolean>(responseVal, HttpStatus.CREATED);
            }
            else{
                return new ResponseEntity<Boolean>(false, HttpStatus.CONFLICT);
            }
        }
        catch(Exception e){
            LOG.log(Level.SEVERE,e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("")
    public ResponseEntity<Boolean> updateTemplate(  @RequestParam(name = "temid", required = true) Integer temid,
                                                    @RequestParam(name = "tname", required = true) String  tname, 
                                                    @RequestParam(name = "tmess", required = true) String  tdesc,
                                                    @RequestParam(name = "tbool", required = true) Boolean tbool
                                                ){
        LOG.info("\nPUT /template/" + temid + ":" + tname);
        try{
            Boolean reponseVal =  templateDao.updateTemplate(temid, tname, tdesc, tbool);
            if(reponseVal){
                return new ResponseEntity<Boolean>(reponseVal, HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch(Exception e){
            LOG.log(Level.SEVERE,e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("")
    public ResponseEntity<Boolean> deleteTemplate(  @RequestParam(name = "temid", required = true) Integer temid
                                                    ){
        LOG.info("\nDELETE /template/" + temid);
        try {
            Boolean reponseVal =  templateDao.deleteTemplate(temid);
            if(reponseVal){
                return new ResponseEntity<Boolean>(reponseVal, HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch(Exception e) {
            LOG.log(Level.SEVERE,e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////
}
