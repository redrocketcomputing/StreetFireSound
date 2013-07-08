/*
 * Copyright (C) 2004 by StreetFire Sound Labs
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: IDLParser.java,v 1.2 2005/02/24 03:03:39 stephen Exp $
 */

package com.streetfiresound.codegenerator.parser;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.streetfiresound.codegenerator.types.AddType;
import com.streetfiresound.codegenerator.types.AndType;
import com.streetfiresound.codegenerator.types.AnyType;
import com.streetfiresound.codegenerator.types.ArrayType;
import com.streetfiresound.codegenerator.types.BooleanType;
import com.streetfiresound.codegenerator.types.CharType;
import com.streetfiresound.codegenerator.types.ConstType;
import com.streetfiresound.codegenerator.types.ContextListType;
import com.streetfiresound.codegenerator.types.ContextType;
import com.streetfiresound.codegenerator.types.DeclarationType;
import com.streetfiresound.codegenerator.types.DoubleType;
import com.streetfiresound.codegenerator.types.EnumType;
import com.streetfiresound.codegenerator.types.ExceptionType;
import com.streetfiresound.codegenerator.types.FloatType;
import com.streetfiresound.codegenerator.types.ForwardDeclarationType;
import com.streetfiresound.codegenerator.types.FunctionType;
import com.streetfiresound.codegenerator.types.HaviType;
import com.streetfiresound.codegenerator.types.HolderType;
import com.streetfiresound.codegenerator.types.InType;
import com.streetfiresound.codegenerator.types.InterfaceHeaderType;
import com.streetfiresound.codegenerator.types.InterfaceType;
import com.streetfiresound.codegenerator.types.LiteralType;
import com.streetfiresound.codegenerator.types.LongLongType;
import com.streetfiresound.codegenerator.types.LongType;
import com.streetfiresound.codegenerator.types.ModuleType;
import com.streetfiresound.codegenerator.types.MultType;
import com.streetfiresound.codegenerator.types.OctetType;
import com.streetfiresound.codegenerator.types.OrType;
import com.streetfiresound.codegenerator.types.RaiseListType;
import com.streetfiresound.codegenerator.types.SequenceType;
import com.streetfiresound.codegenerator.types.ShiftType;
import com.streetfiresound.codegenerator.types.ShortType;
import com.streetfiresound.codegenerator.types.StringType;
import com.streetfiresound.codegenerator.types.StructType;
import com.streetfiresound.codegenerator.types.SwitchType;
import com.streetfiresound.codegenerator.types.TypedefType;
import com.streetfiresound.codegenerator.types.ULongLongType;
import com.streetfiresound.codegenerator.types.ULongType;
import com.streetfiresound.codegenerator.types.UShortType;
import com.streetfiresound.codegenerator.types.UnaryType;
import com.streetfiresound.codegenerator.types.UnionStructType;
import com.streetfiresound.codegenerator.types.UnionType;
import com.streetfiresound.codegenerator.types.VoidType;
import com.streetfiresound.codegenerator.types.WCharType;
import com.streetfiresound.codegenerator.types.WStringType;
import com.streetfiresound.codegenerator.types.XorType;




public class IDLParser implements IDLParserConstants {

   static String tempUnionName = "";

   static ArrayList moduleList = CodeGenerator.moduleList;
   static HashMap dataTypeList = CodeGenerator.dataTypeList;
   static HashMap constList = CodeGenerator.constList;


	  private static HaviType getPragmaType(String type)
	  {
			try
			{
        Class newClass = Class.forName("com.streetfiresound.codegenerator.types."+type);

        Class[] parameter = new Class[0];

        Object[] parameterList = new Object[0];

        Constructor constructor = newClass.getConstructor(parameter);

				HaviType haviType = (HaviType)constructor.newInstance(parameterList);

        return haviType;
			}
			catch(Exception e)
			{
				return new LiteralType();
			}


	  }


/*
  public static ArrayList getModuleList()
  {
      return moduleList;
  }

  public static HashMap getDataTypeList()
  {
      return dataTypeList;
  }


  public static HashMap getConstList()
  {
        return constList;
  }

*/

  public static void main(String args[]) {
    IDLParser parser;
    if (args.length == 0) {
      System.out.println("StreetFire IDLParser Version 1.0: Reading from standard input . . .");
      parser = new IDLParser(System.in);
    } else if (args.length == 1) {
      System.out.println("StreetFire IDLParser Version 1.0: Reading from file " + args[0] + " . . .");
      try {
        parser = new IDLParser(new java.io.FileInputStream(args[0]));
      } catch (java.io.FileNotFoundException e) {
        System.out.println("StreetFire IDLParser Version 1.0: File " + args[0] + " not found.");
        return;
      }
    } else {
      System.out.println("Usage is one of:");
      System.out.println("         java IDLParser < inputfile");
      System.out.println("OR");
      System.out.println("         java IDLParser inputfile");
      return;
    }
    try {
      parser.specification();
      System.out.println("StreetFire IDLParser Version 1.0: Java program parsed successfully.");
    } catch (ParseException e) {
      System.out.println("StreetFire IDLParser Version 1.0: Encountered errors during parse.");
    }
  }

/* Production 1 Every document is a ContextType, it contains a list of ConstructType*/
  static final public HaviType specification() throws ParseException {

        ContextType ct = new ContextType();

        HaviType ctype;

    label_1:
    while (true) {

      ctype = definition();

      ct.addChild(ctype);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 8:
      case 11:
      case 15:
      case 32:
      case 43:
      case 44:
      case 48:
      case 58:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
    }
     {if (true) return ct;}
    throw new Error("Missing return statement in function");
  }

/* Production 2 */
  static final public HaviType definition() throws ParseException {
    HaviType dataType = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 32:
    case 43:
    case 44:
    case 48:

      dataType = type_dcl();
      jj_consume_token(7);
                                  {if (true) return dataType;}
      break;
    case 15:
      dataType = const_dcl();
      jj_consume_token(7);
                                  {if (true) return dataType;}
      break;
    case 58:
      dataType = except_dcl();
      jj_consume_token(7);
                                  {if (true) return dataType;}
      break;
    case 11:
      dataType = interfacex();
      jj_consume_token(7);
                                  {if (true) return dataType;}
      break;
    case 8:
      dataType = module();
      jj_consume_token(7);
                                  {if (true) return dataType;}
      break;
    default:
      jj_la1[1] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 3 */
  static final public HaviType module() throws ParseException {
        ModuleType mt = new ModuleType();
        HaviType temp;
    jj_consume_token(8);
    temp = identifier();
        //mt.addChild(temp);
        mt.setTypeName(   ((LiteralType)temp).getTypeName() );
    jj_consume_token(9);
    label_2:
    while (true) {
      temp = definition();
                         mt.addChild(temp);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 8:
      case 11:
      case 15:
      case 32:
      case 43:
      case 44:
      case 48:
      case 58:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_2;
      }
    }
    jj_consume_token(10);
        moduleList.add(  mt.getTypeName().toLowerCase()   );
        {if (true) return mt;}
    throw new Error("Missing return statement in function");
  }

/* Production 4 */
  static final public HaviType interfacex() throws ParseException {
        HaviType dataType;
    if (jj_2_1(3)) {
      dataType = interface_dcl();
                                  {if (true) return dataType;}
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 11:
        dataType = forward_dcl();
                                  {if (true) return dataType;}
        break;
      default:
        jj_la1[3] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    throw new Error("Missing return statement in function");
  }

/* Production 5 */
  static final public HaviType interface_dcl() throws ParseException {
        InterfaceType it = new InterfaceType();
        InterfaceHeaderType header;
        ArrayList list = null;
	    header = interface_header();
                      it.setInterfaceHeaderType(header);
    jj_consume_token(9);
    list = interface_body();
    jj_consume_token(10);
        it.setChildList(list);
        {if (true) return it;}
    throw new Error("Missing return statement in function");
  }

/* Production 6 */
  static final public HaviType forward_dcl() throws ParseException {
        HaviType forwardName;
    jj_consume_token(11);
    forwardName = identifier();
        ForwardDeclarationType ft = new ForwardDeclarationType();
        ft.addChild(forwardName);
        {if (true) return ft;}
    throw new Error("Missing return statement in function");
  }

/* Production 7 */
  static final public InterfaceHeaderType interface_header() throws ParseException {
        HaviType identifier;
        ArrayList list = null;
    jj_consume_token(11);
    identifier = identifier();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 12:
      list = inheritance_spec();
      break;
    default:
      jj_la1[4] = jj_gen;
      ;
    }
        InterfaceHeaderType iht = new InterfaceHeaderType();
        iht.setTypeName(identifier.getTypeName());

        iht.setChildList(list);
        {if (true) return iht;}
    throw new Error("Missing return statement in function");
  }

/* Production 8 */
  static final public ArrayList interface_body() throws ParseException {
        ArrayList list = new ArrayList();
        HaviType dataType;
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 14:
      case 15:
      case 32:
      case 33:
      case 34:
      case 35:
      case 36:
      case 37:
      case 38:
      case 39:
      case 40:
      case 41:
      case 42:
      case 43:
      case 44:
      case 48:
      case 49:
      case 52:
      case 53:
      case 56:
      case 57:
      case 58:
      case 59:
      case 60:
      case ID:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_3;
      }
      dataType = export();
                          list.add(dataType);
    }
   {if (true) return list;}
    throw new Error("Missing return statement in function");
  }

/* Production 9 */
  static final public HaviType export() throws ParseException {
        HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 32:
    case 43:
    case 44:
    case 48:
      dataType = type_dcl();
      jj_consume_token(7);
                                  {if (true) return dataType;}
      break;
    case 15:
      dataType = const_dcl();
      jj_consume_token(7);
                                  {if (true) return dataType;}
      break;
    case 58:
      dataType = except_dcl();
      jj_consume_token(7);
                                  {if (true) return dataType;}
      break;
    case 56:
    case 57:
      dataType = attr_dcl();
      jj_consume_token(7);
                                  {if (true) return dataType;}
      break;
    case 14:
    case 33:
    case 34:
    case 35:
    case 36:
    case 37:
    case 38:
    case 39:
    case 40:
    case 41:
    case 42:
    case 49:
    case 52:
    case 53:
    case 59:
    case 60:
    case ID:
      dataType = op_dcl();
      jj_consume_token(7);
                                  {if (true) return dataType;}
      break;
    default:
      jj_la1[6] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 10 */
  static final public ArrayList inheritance_spec() throws ParseException {
        ArrayList list = new ArrayList();
        HaviType sname;

        String temp=new String();
    jj_consume_token(12);
    sname = scoped_name();
                             list.add(sname);
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 13:
        ;
        break;
      default:
        jj_la1[7] = jj_gen;
        break label_4;
      }
      jj_consume_token(13);
      sname = scoped_name();
                                                                              list.add(sname);
    }
    {if (true) return list;}
    throw new Error("Missing return statement in function");
  }

/* Production 11 */
  static final public HaviType scoped_name() throws ParseException {
        HaviType scopeName;

			  String pragmaType;

      String temp = new String();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 14:
      jj_consume_token(14);
      break;
    default:
      jj_la1[8] = jj_gen;
      ;
    }
    scopeName = identifier();
		pragmaType = scopeName.getTypeName();


    temp = scopeName.getTypeName();

    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 14:
        ;
        break;
      default:
        jj_la1[9] = jj_gen;
        break label_5;
      }
      jj_consume_token(14);
      scopeName = identifier();
		  temp = scopeName.getTypeName();

    }
    {
    		if (true)
    		{
					HaviType htype = getPragmaType(pragmaType);
					htype.setTypeName(temp);
					return htype;
// 			 		return new LiteralType(temp);
    		}

    }
    throw new Error("Missing return statement in function");
  }

/* Production 12 */
  static final public HaviType const_dcl() throws ParseException {
        ConstType ct = new ConstType();
        HaviType temp;
        String typeName;
    jj_consume_token(15);
    temp = const_type();
        typeName = temp.getTypeName();
        ct.setDataType(temp);
    temp = identifier();
                       ct.setTypeName(temp.getTypeName());
    jj_consume_token(16);
    temp = const_exp();
                         ct.setValue(temp.getTypeName());
      ArrayList al = (ArrayList) constList.get(typeName);
      if(al == null)
      {
        al = new ArrayList();
        constList.put(typeName, al);
      }

      al.add(ct);
      {if (true) return ct;}
    throw new Error("Missing return statement in function");
  }

/* Production 13 */
  static final public HaviType const_type() throws ParseException {
        HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 35:
    case 36:
    case 37:
      dataType = integer_type();
                                  {if (true) return dataType;}
      break;
    case 38:
    case 39:
      dataType = char_type();
                                  {if (true) return dataType;}
      break;
    case 40:
      dataType = boolean_type();
                                  {if (true) return dataType;}
      break;
    case 33:
    case 34:
      dataType = floating_pt_type();
                                  {if (true) return dataType;}
      break;
    case 52:
    case 53:
      dataType = string_type();
                                  {if (true) return dataType;}
      break;
    case 14:
    case ID:
      dataType = scoped_name();
                                  {if (true) return dataType;}
      break;
    case 41:
      dataType = octet_type();
                                  {if (true) return dataType;}
      break;
    default:
      jj_la1[10] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 14 */
  static final public HaviType const_exp() throws ParseException {
        HaviType temp;
    temp = unary_expr();
                          {if (true) return temp;}
    throw new Error("Missing return statement in function");
  }

/* Production 15 */
  static final public HaviType or_expr() throws ParseException {
        OrType lt = new OrType();
        HaviType temp;
    temp = xor_expr();
                    lt.addChild(temp);
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 17:
        ;
        break;
      default:
        jj_la1[11] = jj_gen;
        break label_6;
      }
      jj_consume_token(17);
      temp = xor_expr();
                          lt.addChild(temp);
    }
    {if (true) return lt;}
    throw new Error("Missing return statement in function");
  }

/* Production 16 */
  static final public HaviType xor_expr() throws ParseException {
        XorType lt = new XorType();
        HaviType temp;
    temp = and_expr();
                    lt.addChild(temp);
    label_7:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 18:
        ;
        break;
      default:
        jj_la1[12] = jj_gen;
        break label_7;
      }
      jj_consume_token(18);
      temp = and_expr();
                          lt.addChild(temp);
    }
    {if (true) return lt;}
    throw new Error("Missing return statement in function");
  }

/* Production 17 */
  static final public HaviType and_expr() throws ParseException {
        AndType lt = new AndType();
        HaviType temp;
    temp = shift_expr();
                          lt.addChild(temp);
    label_8:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 19:
        ;
        break;
      default:
        jj_la1[13] = jj_gen;
        break label_8;
      }
      jj_consume_token(19);
      temp = shift_expr();
                            lt.addChild(temp);
    }
    {if (true) return lt;}
    throw new Error("Missing return statement in function");
  }

/* Production 18 */
  static final public HaviType shift_expr() throws ParseException {
        ShiftType lt = new ShiftType();
        HaviType temp;
    temp = addChild_expr();
                           lt.addChild(temp);
    label_9:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 20:
      case 21:
        ;
        break;
      default:
        jj_la1[14] = jj_gen;
        break label_9;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 20:
        jj_consume_token(20);
            lt.addChild(new LiteralType(">>"));
        break;
      case 21:
        jj_consume_token(21);
                                                        lt.addChild(new LiteralType("<<"));
        break;
      default:
        jj_la1[15] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      temp = addChild_expr();
                                                                                                                         lt.addChild(temp);
    }
    {if (true) return lt;}
    throw new Error("Missing return statement in function");
  }

/* Production 19 */
  static final public HaviType addChild_expr() throws ParseException {
        AddType lt = new AddType();
        HaviType temp;
    temp = mult_expr();
                     lt.addChild(temp);
    label_10:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 22:
      case 23:
        ;
        break;
      default:
        jj_la1[16] = jj_gen;
        break label_10;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 22:
        jj_consume_token(22);
           lt.addChild(new LiteralType("+"));
        break;
      case 23:
        jj_consume_token(23);
                                                      lt.addChild(new LiteralType("-"));
        break;
      default:
        jj_la1[17] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      temp = mult_expr();
                                                                                                                   lt.addChild(temp);
    }
    {if (true) return lt;}
    throw new Error("Missing return statement in function");
  }

/* Production 20 */
  static final public HaviType mult_expr() throws ParseException {
        MultType lt = new MultType();
        HaviType temp;
    temp = unary_expr();
                         lt.addChild(temp);
    label_11:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 24:
      case 25:
      case 26:
        ;
        break;
      default:
        jj_la1[18] = jj_gen;
        break label_11;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 24:
        jj_consume_token(24);
           lt.addChild(new LiteralType("*"));
        break;
      case 25:
        jj_consume_token(25);
                                                      lt.addChild(new LiteralType("/"));
        break;
      case 26:
        jj_consume_token(26);
                                                                                                 lt.addChild(new LiteralType("%"));
        break;
      default:
        jj_la1[19] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      temp = unary_expr();
                                                                                                                                                           lt.addChild(temp);
    }
    {if (true) return lt;}
    throw new Error("Missing return statement in function");
  }

/* Production 21 */
  static final public HaviType unary_expr() throws ParseException {
        UnaryType ut = new UnaryType();
        HaviType temp;

        String tempStr = new String();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 22:
    case 23:
    case 27:
      temp = unary_operator();
                            tempStr += temp.getTypeName();
      break;
    default:
      jj_la1[20] = jj_gen;
      ;
    }
    temp = primary_expr();
                                                                                       tempStr += temp.getTypeName(); {if (true) return new LiteralType(tempStr);}
    throw new Error("Missing return statement in function");
  }

/* Production 22 */
  static final public HaviType unary_operator() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 23:
      jj_consume_token(23);
          {if (true) return new LiteralType("-");}
      break;
    case 22:
      jj_consume_token(22);
          {if (true) return new LiteralType("+");}
      break;
    case 27:
      jj_consume_token(27);
          {if (true) return new LiteralType("~");}
      break;
    default:
      jj_la1[21] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 23 */
  static final public HaviType primary_expr() throws ParseException {
        HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 14:
    case ID:
      dataType = scoped_name();
                                  {if (true) return dataType;}
      break;
    case 30:
    case 31:
    case OCTALINT:
    case DECIMALINT:
    case HEXADECIMALINT:
    case FLOATONE:
    case FLOATTWO:
    case CHARACTER:
    case STRING:
      dataType = literal();
                                  {if (true) return dataType;}
      break;
    case 28:
      jj_consume_token(28);
      dataType = const_exp();
      jj_consume_token(29);
                                 {if (true) return dataType;}
      break;
    default:
      jj_la1[22] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 24 */
  static final public HaviType literal() throws ParseException {
        HaviType literal;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OCTALINT:
    case DECIMALINT:
    case HEXADECIMALINT:
      literal = integer_literal();
                                  {if (true) return literal;}
      break;
    case STRING:
      literal = string_literal();
                                  {if (true) return literal;}
      break;
    case CHARACTER:
      literal = character_literal();
                                  {if (true) return literal;}
      break;
    case FLOATONE:
    case FLOATTWO:
      literal = floating_pt_literal();
                                  {if (true) return literal;}
      break;
    case 30:
    case 31:
      literal = boolean_literal();
                                  {if (true) return literal;}
      break;
    default:
      jj_la1[23] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 25 */
  static final public HaviType boolean_literal() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 30:
      jj_consume_token(30);
                  {if (true) return new LiteralType("TRUE");}
      break;
    case 31:
      jj_consume_token(31);
                  {if (true) return new LiteralType("FALSE");}
      break;
    default:
      jj_la1[24] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 26 */
  static final public HaviType positive_int_const() throws ParseException {
        HaviType value;
    value = const_exp();
                          {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

/* Production 27 */
  static final public HaviType type_dcl() throws ParseException {



    HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 32:

      jj_consume_token(32);


      dataType = type_declarator();

        TypedefType tt = new TypedefType();


       tt.addChild(dataType);

        {if (true) return tt;}
      break;
    case 43:
      dataType = struct_type();
                                  {if (true) return dataType;}
      break;
    case 44:
      dataType = union_type();
                                  {if (true) return dataType;}
      break;
    case 48:
      dataType = enum_type();
                                  {if (true) return dataType;}
      break;
    default:
      jj_la1[25] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 28 */
  static final public HaviType type_declarator() throws ParseException {

        HaviType spec;

        ArrayList decl;

    spec = type_spec();

    decl = declarators();

        HaviType hType = spec;

        HaviType finalType = hType;

        while(hType != null)
        {
                finalType = hType;

                if(finalType instanceof StructType || finalType instanceof UnionType || finalType instanceof EnumType)
                  break;

                hType = (HaviType)dataTypeList.get(hType.getTypeName());

        }

//        if(finalType instanceof LiteralType)
//				System.out.println();
//                {if (true) throw new ParseException("typedef - Scope name not found:" + finalType.getTypeName());}	//temporary remove
//        else
//        {

                Iterator iter = decl.iterator();

                while(iter.hasNext())
                {
                        HaviType type =  (HaviType) iter.next();
                        String name = type.getTypeName();
                        if(finalType instanceof SequenceType ||  type instanceof ArrayType)
                        {
                          StructType temp = new StructType();
                          temp.setTypeName(name);
                          finalType = temp;
                        }
                        dataTypeList.put(   name, finalType);
                }


//        }

        DeclarationType dt = new DeclarationType();

        dt.setDataType(spec);

        dt.setTypeName(spec.getTypeName());

        dt.setChildList(decl);


        {
        	if (true)
        	return dt;
        }
    throw new Error("Missing return statement in function");
  }

/* Production 29 */
  static final public HaviType type_spec() throws ParseException {
        HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 14:
    case 33:
    case 34:
    case 35:
    case 36:
    case 37:
    case 38:
    case 39:
    case 40:
    case 41:
    case 42:
    case 49:
    case 52:
    case 53:
    case ID:
      dataType = simple_type_spec();
                                  {if (true) return dataType;}
      break;
    case 43:
    case 44:
    case 48:
      dataType = constr_type_spec();
                                  {if (true) return dataType;}
      break;
    default:
      jj_la1[26] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 30 */
  static final public HaviType simple_type_spec() throws ParseException {
        HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 33:
    case 34:
    case 35:
    case 36:
    case 37:
    case 38:
    case 39:
    case 40:
    case 41:
    case 42:
      dataType = base_type_spec();
                                  {if (true) return dataType;}
      break;
    case 49:
    case 52:
    case 53:
      dataType = template_type_spec();
                                  {if (true) return dataType;}
      break;
    case 14:
    case ID:
      dataType = scoped_name();
                                  {if (true) return dataType;}
      break;
    default:
      jj_la1[27] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 31 */
  static final public HaviType base_type_spec() throws ParseException {
        HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 33:
    case 34:
      dataType = floating_pt_type();
                                  {if (true) return dataType;}
      break;
    case 35:
    case 36:
    case 37:
      dataType = integer_type();
                                  {if (true) return dataType;}
      break;
    case 38:
    case 39:
      dataType = char_type();
                                  {if (true) return dataType;}
      break;
    case 40:
      dataType = boolean_type();
                                  {if (true) return dataType;}
      break;
    case 41:
      dataType = octet_type();
                                  {if (true) return dataType;}
      break;
    case 42:
      dataType = any_type();
                                  {if (true) return dataType;}
      break;
    default:
      jj_la1[28] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 32 */
  static final public HaviType template_type_spec() throws ParseException {
        HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 49:
      dataType = sequence_type();
                                  {if (true) return dataType;}
      break;
    case 52:
    case 53:
      dataType = string_type();
                                  {if (true) return dataType;}
      break;
    default:
      jj_la1[29] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 33 */
  static final public HaviType constr_type_spec() throws ParseException {
        HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 43:
      dataType = struct_type();
                                  {if (true) return dataType;}
      break;
    case 44:
      dataType = union_type();
                                  {if (true) return dataType;}
      break;
    case 48:
      dataType = enum_type();
                                  {if (true) return dataType;}
      break;
    default:
      jj_la1[30] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 34 */
  static final public ArrayList declarators() throws ParseException {
        ArrayList list = new ArrayList();
        HaviType temp;
    temp = declarator();
                      list.add(temp);
    label_12:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 13:
        ;
        break;
      default:
        jj_la1[31] = jj_gen;
        break label_12;
      }
      jj_consume_token(13);
      temp = declarator();
                                                                    list.add(temp);
    }
    {if (true) return list;}
    throw new Error("Missing return statement in function");
  }

/* Production 35 */
  static final public HaviType declarator() throws ParseException {
        HaviType dataType;
    if (jj_2_2(2)) {
      dataType = complex_declarator();
                                          {if (true) return dataType;}
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ID:
        dataType = simple_declarator();
                                          {if (true) return dataType;}
        break;
      default:
        jj_la1[32] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    throw new Error("Missing return statement in function");
  }

/* Production 36 */
  static final public HaviType simple_declarator() throws ParseException {
        HaviType temp;
    temp = identifier();
                          {if (true) return temp;}
    throw new Error("Missing return statement in function");
  }

/* Production 37 */
  static final public HaviType complex_declarator() throws ParseException {
        HaviType dataType;
    dataType = array_declarator();
                                  {if (true) return dataType;}
    throw new Error("Missing return statement in function");
  }

/* Production 38 */
  static final public HaviType floating_pt_type() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 33:
      jj_consume_token(33);
                  {if (true) return new FloatType();}
      break;
    case 34:
      jj_consume_token(34);
                  {if (true) return new DoubleType();}
      break;
    default:
      jj_la1[33] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 39 */
  static final public HaviType integer_type() throws ParseException {
        HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 35:
    case 36:
      dataType = signed_int();
                          {if (true) return dataType;}
      break;
    case 37:
      dataType = unsigned_int();
                            {if (true) return dataType;}
      break;
    default:
      jj_la1[34] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 40 */
  static final public HaviType signed_int() throws ParseException {
        HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 35:
      dataType = signed_long_int();
                                  {if (true) return dataType;}
      break;
    case 36:
      dataType = signed_short_int();
                                  {if (true) return dataType;}
      break;
    default:
      jj_la1[35] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 41 */
  static final public HaviType signed_long_int() throws ParseException {
        String value;
    jj_consume_token(35);
          value = "long";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 35:
      jj_consume_token(35);
      value += " long";
      break;
    default:
      jj_la1[36] = jj_gen;
      ;
    }
        if(value.equals("long long"))
                {if (true) return new LongLongType();}
        else
                {if (true) return new LongType();}
    throw new Error("Missing return statement in function");
  }

/* Production 42 */
  static final public HaviType signed_short_int() throws ParseException {
    jj_consume_token(36);
                  {if (true) return new ShortType();}
    throw new Error("Missing return statement in function");
  }

/* Production 43 */
  static final public HaviType unsigned_int() throws ParseException {
        HaviType dataType;
    if (jj_2_3(2)) {
      dataType = unsigned_long_int();
                                  {if (true) return dataType;}
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 37:
        dataType = unsigned_short_int();
                                  {if (true) return dataType;}
        break;
      default:
        jj_la1[37] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    throw new Error("Missing return statement in function");
  }

/* Production 44 */
  static final public HaviType unsigned_long_int() throws ParseException {
        String name;
    jj_consume_token(37);
    jj_consume_token(35);
                      name = "long";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 35:
      jj_consume_token(35);
                                                   name +=" long";
      break;
    default:
      jj_la1[38] = jj_gen;
      ;
    }
        if(name.equals("long long"))
            {if (true) return new ULongLongType();}
        else
            {if (true) return new ULongType();}
    throw new Error("Missing return statement in function");
  }

/* Production 45 */
  static final public HaviType unsigned_short_int() throws ParseException {
    jj_consume_token(37);
    jj_consume_token(36);
                          {if (true) return new UShortType();}
    throw new Error("Missing return statement in function");
  }

/* Production 46 */
  static final public HaviType char_type() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 38:
      jj_consume_token(38);
                  {if (true) return new CharType();}
      break;
    case 39:
      jj_consume_token(39);
                  {if (true) return new WCharType();}
      break;
    default:
      jj_la1[39] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 47 */
  static final public HaviType boolean_type() throws ParseException {
    jj_consume_token(40);
                  {if (true) return new BooleanType();}
    throw new Error("Missing return statement in function");
  }

/* Production 48 */
  static final public HaviType octet_type() throws ParseException {
    jj_consume_token(41);
                  {if (true) return new OctetType();}
    throw new Error("Missing return statement in function");
  }

/* Production 49 */
  static final public HaviType any_type() throws ParseException {
    jj_consume_token(42);
          {if (true) return new AnyType();}
    throw new Error("Missing return statement in function");
  }

/* Production 50 */
  static final public HaviType struct_type() throws ParseException {
        ArrayList list;
        HaviType structName;
        StructType st = new StructType();
    jj_consume_token(43);
    structName = identifier();
        st.setTypeName(   ((LiteralType)structName).getTypeName() );
        dataTypeList.put(  st.getTypeName() , st);
    jj_consume_token(9);
    list = member_list();
        st.setChildList(list);
    jj_consume_token(10);
        {if (true) return st;}
    throw new Error("Missing return statement in function");
  }

/* Production 51 */
  static final public ArrayList member_list() throws ParseException {
        ArrayList list = new ArrayList();
        HaviType temp;
    label_13:
    while (true) {
      temp = member();
                     list.add(temp);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 14:
      case 33:
      case 34:
      case 35:
      case 36:
      case 37:
      case 38:
      case 39:
      case 40:
      case 41:
      case 42:
      case 43:
      case 44:
      case 48:
      case 49:
      case 52:
      case 53:
      case ID:
        ;
        break;
      default:
        jj_la1[40] = jj_gen;
        break label_13;
      }
    }
    {if (true) return list;}
    throw new Error("Missing return statement in function");
  }

/* Production 52 */
  static final public HaviType member() throws ParseException {
        HaviType temp;
        HaviType spec;
        ArrayList decl;
    spec = type_spec();
    decl = declarators();
    jj_consume_token(7);
                String typeName = spec.getTypeName();

                temp = spec;

                if(spec  instanceof LiteralType)
                {
                        spec = (HaviType) dataTypeList.get(spec.getTypeName());

                        if(spec == null)
                                spec = temp;
                                //throw new ParseException("struct - Scope name not found:" + typeName);

                }

                DeclarationType dt = new DeclarationType();
                dt.setDataType(spec);
                dt.setTypeName(spec.getTypeName());
                dt.setChildList(decl);
                {if (true) return dt;}
    throw new Error("Missing return statement in function");
  }

/* Production 53 */
  static final public HaviType union_type() throws ParseException {
        UnionType ut = new UnionType();
        HaviType temp;

        ArrayList switchList;
    jj_consume_token(44);
    temp = identifier();
        tempUnionName = temp.getTypeName();
        dataTypeList.put(temp.getTypeName(), ut);
        ut.setTypeName(temp.getTypeName());
    jj_consume_token(45);
    jj_consume_token(28);
    temp = switch_type_spec();
        String typeName = temp.getTypeName();

//	temporary change - instead of throw exception, reassign the type to temp from temptype

        if(temp instanceof LiteralType)
        {
				HaviType tempType = temp;
                temp = (HaviType) dataTypeList.get(typeName);
                if(temp == null)
                	temp = tempType;
//                        {if (true) throw new ParseException("union  scoped name not found:" + typeName);}
        }

        ut.setDataType(temp);
    jj_consume_token(29);
    jj_consume_token(9);
    switchList = switch_body();
    jj_consume_token(10);
        ut.setChildList(switchList);
        tempUnionName="";
        {if (true) return ut;}
    throw new Error("Missing return statement in function");
  }

/* Production 54 */
  static final public HaviType switch_type_spec() throws ParseException {
        HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 35:
    case 36:
    case 37:
      dataType = integer_type();
                                  {if (true) return dataType;}
      break;
    case 38:
    case 39:
      dataType = char_type();
                                  {if (true) return dataType;}
      break;
    case 40:
      dataType = boolean_type();
                                  {if (true) return dataType;}
      break;
    case 48:
      dataType = enum_type();
                                  {if (true) return dataType;}
      break;
    case 14:
    case ID:
      dataType = scoped_name();
                                  {if (true) return dataType;}
      break;
    default:
      jj_la1[41] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 55 */
  static final public ArrayList switch_body() throws ParseException {
        ArrayList list = new ArrayList();
        HaviType dataType;
    label_14:
    while (true) {
      dataType = casex();
                       list.add(dataType);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 46:
      case 47:
        ;
        break;
      default:
        jj_la1[42] = jj_gen;
        break label_14;
      }
    }
    {if (true) return list;}
    throw new Error("Missing return statement in function");
  }

/* Production 56 */
  static final public HaviType casex() throws ParseException {
        HaviType element;
        SwitchType st = new SwitchType();
        String temp;
        int count = 0;
    label_15:
    while (true) {
      temp = case_label();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 46:
      case 47:
        ;
        break;
      default:
        jj_la1[43] = jj_gen;
        break label_15;
      }
    }
    label_16:
    while (true) {
      element = element_spec();
      jj_consume_token(7);
                 st.addChild(element);
                 if(count++ > 1)
                 {
                        System.err.println("Warning! Not fully compatible with IDL standard");
                }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 14:
      case 33:
      case 34:
      case 35:
      case 36:
      case 37:
      case 38:
      case 39:
      case 40:
      case 41:
      case 42:
      case 43:
      case 44:
      case 48:
      case 49:
      case 52:
      case 53:
      case ID:
        ;
        break;
      default:
        jj_la1[44] = jj_gen;
        break label_16;
      }
    }
        st.setLabel(temp);

        UnionStructType structType = new UnionStructType ();
        structType.setTypeName(temp.substring(0,1)+temp.substring(1).toLowerCase()+tempUnionName);
        dataTypeList.put(structType.getTypeName(), structType);


        {if (true) return st;}
    throw new Error("Missing return statement in function");
  }

/* Production 57 */
  static final public String case_label() throws ParseException {
        HaviType temp;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 46:
      jj_consume_token(46);
      temp = const_exp();
      jj_consume_token(12);
                                  {if (true) return temp.getTypeName();}
      break;
    case 47:
      jj_consume_token(47);
      jj_consume_token(12);
                  {if (true) return "default";}
      break;
    default:
      jj_la1[45] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 58 */
  static final public HaviType element_spec() throws ParseException {
        DeclarationType dt = new DeclarationType();
        HaviType temp;
	    temp = type_spec();
        String typeName = temp.getTypeName();


//temporary change - instead of throw parseexception, reassign temp with temptype
        if(temp instanceof LiteralType)
        {
        		HaviType temptype = temp;
                temp = (HaviType) dataTypeList.get(typeName);
                if(temp == null)
                {
                	temp = temptype;
//                   {if (true) throw new ParseException("switch type scoped name not found:" + typeName);}
                }

        }

        dt.setDataType(temp);
        dt.setTypeName(temp.getTypeName());
	    temp = declarator();
        dt.addChild(temp);
        {if (true) return dt;}
    throw new Error("Missing return statement in function");
  }

/* Production 59 */
  static final public HaviType enum_type() throws ParseException {
        EnumType eType = new EnumType();
        HaviType temp;
    jj_consume_token(48);
    temp = identifier();
        eType.setTypeName(((LiteralType)temp).getTypeName() );
    jj_consume_token(9);
    temp = enumerator();
        eType.addChild(temp);
    label_17:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 13:
        ;
        break;
      default:
        jj_la1[46] = jj_gen;
        break label_17;
      }
      jj_consume_token(13);
      temp = enumerator();
        eType.addChild(temp);
    }
    jj_consume_token(10);
        dataTypeList.put(eType.getTypeName(), eType);

        {if (true) return eType;}
    throw new Error("Missing return statement in function");
  }

/* Production 60 */
  static final public HaviType enumerator() throws ParseException {
        HaviType vName;
    vName = identifier();
                          {if (true) return vName;}
    throw new Error("Missing return statement in function");
  }

/* Production 61 */
  static final public HaviType sequence_type() throws ParseException {
        SequenceType st = new SequenceType();
        HaviType temp;
        st.setValue("0");
    jj_consume_token(49);
    jj_consume_token(50);
    temp = simple_type_spec();
        String typeName = temp.getTypeName();


	//temporary change - instead of throw parseexception, reassign temp with temptype;
	  	if(temp instanceof LiteralType)
		{
			HaviType temptype = temp;
			temp = (HaviType) dataTypeList.get(typeName);

			if(temp == null)
				temp = temptype;
	//			throw new ParseException("sequence scoped name not found:" + typeName);

		}

        st.setDataType(temp);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 13:
      jj_consume_token(13);
      temp = positive_int_const();
                                   st.setValue(temp.getTypeName());
      break;
    default:
      jj_la1[47] = jj_gen;
      ;
    }
    jj_consume_token(51);
        {if (true) return st;}
    throw new Error("Missing return statement in function");
  }

/* Production 62  Modified by George on 11-20-03  addChild one more data type "wstring"  */
  static final public HaviType string_type() throws ParseException {
        HaviType value = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 52:
      jj_consume_token(52);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 50:
        jj_consume_token(50);
        value = positive_int_const();
        jj_consume_token(51);
        break;
      default:
        jj_la1[48] = jj_gen;
        ;
      }
        StringType st = new StringType();
        if(value != null)
                st.setTypeName(value.getTypeName());
        {if (true) return st;}
      break;
    case 53:
      jj_consume_token(53);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 50:
        jj_consume_token(50);
        value = positive_int_const();
        jj_consume_token(51);
        break;
      default:
        jj_la1[49] = jj_gen;
        ;
      }
        WStringType wst = new WStringType();
        //if(value != null)
                //wst.addChild(value);
        {if (true) return wst;}
      break;
    default:
      jj_la1[50] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 63 */
  static final public HaviType array_declarator() throws ParseException {
        ArrayType at = new ArrayType();
        HaviType temp;
    temp = identifier();
        at.setTypeName(temp.getTypeName());
    label_18:
    while (true) {
      temp = fixed_array_size();
                              at.addChild(temp);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 54:
        ;
        break;
      default:
        jj_la1[51] = jj_gen;
        break label_18;
      }
    }
        {if (true) return at;}
    throw new Error("Missing return statement in function");
  }

/* Production 64 */
  static final public HaviType fixed_array_size() throws ParseException {
        HaviType value;
    jj_consume_token(54);
    value = positive_int_const();
    jj_consume_token(55);
                                          {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

/* Production 65 */
  static final public HaviType attr_dcl() throws ParseException {
        DeclarationType dt = new DeclarationType();
        HaviType temp;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 56:
      jj_consume_token(56);
      break;
    default:
      jj_la1[52] = jj_gen;
      ;
    }
    jj_consume_token(57);
    temp = param_type_spec();
        dt.setDataType(temp);
        dt.setTypeName(temp.getTypeName());
    temp = simple_declarator();
                            dt.addChild(temp);
    label_19:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 13:
        ;
        break;
      default:
        jj_la1[53] = jj_gen;
        break label_19;
      }
      jj_consume_token(13);
      temp = simple_declarator();
	  dt.addChild(temp);
    }
    {if (true) return dt;}
    throw new Error("Missing return statement in function");
  }

/* Production 66 */
  static final public HaviType except_dcl() throws ParseException {
        ExceptionType et = new ExceptionType();
        HaviType temp;
    jj_consume_token(58);
    temp = identifier();
                                  et.setTypeName(temp.getTypeName());
    jj_consume_token(9);
    label_20:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 14:
      case 33:
      case 34:
      case 35:
      case 36:
      case 37:
      case 38:
      case 39:
      case 40:
      case 41:
      case 42:
      case 43:
      case 44:
      case 48:
      case 49:
      case 52:
      case 53:
      case ID:
        ;
        break;
      default:
        jj_la1[54] = jj_gen;
        break label_20;
      }
      temp = member();
                        et.addChild(temp);
    }
    jj_consume_token(10);
        {if (true) return et;}
    throw new Error("Missing return statement in function");
  }

/* Production 67 */
  static final public HaviType op_dcl() throws ParseException {
        FunctionType ft = new FunctionType();

        HaviType temp;

        ArrayList list = null;
        RaiseListType raiseList = null;
        ContextListType contextList = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 59:
      op_attribute();
      break;
    default:
      jj_la1[55] = jj_gen;
      ;
    }
    temp = op_type_spec();
      ft.setReturnType(temp);
    temp = identifier();
       ft.setTypeName(temp.getTypeName());
    list = parameter_dcls();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 64:
      raiseList = raises_expr();
      break;
    default:
      jj_la1[56] = jj_gen;
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 65:
      contextList = context_expr();
      break;
    default:
      jj_la1[57] = jj_gen;
      ;
    }
        ft.setChildList(list);

        ft.setRaiseListType(raiseList);

        ft.setContextListType(contextList);

        {if (true) return ft;}
    throw new Error("Missing return statement in function");
  }

/* Production 68 */
  static final public void op_attribute() throws ParseException {
    jj_consume_token(59);
  }

/* Production 69 */
  static final public HaviType op_type_spec() throws ParseException {
        HaviType dataType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 14:
    case 33:
    case 34:
    case 35:
    case 36:
    case 37:
    case 38:
    case 39:
    case 40:
    case 41:
    case 42:
    case 49:
    case 52:
    case 53:
    case ID:
      dataType = param_type_spec();
                                  {if (true) return dataType;}
      break;
    case 60:
      jj_consume_token(60);
                                  {if (true) return new VoidType();}
      break;
    default:
      jj_la1[58] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 70 */
  static final public ArrayList parameter_dcls() throws ParseException {
        ArrayList list = new ArrayList();
        HaviType temp;
    jj_consume_token(28);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 61:
    case 62:
    case 63:
      temp = param_dcl();
                          list.add(temp);
      label_21:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 13:
          ;
          break;
        default:
          jj_la1[59] = jj_gen;
          break label_21;
        }
        jj_consume_token(13);
        temp = param_dcl();
        list.add(temp);
      }
      break;
    default:
      jj_la1[60] = jj_gen;
      ;
    }
    jj_consume_token(29);
    {if (true) return list;}
    throw new Error("Missing return statement in function");
  }

/* Production 71 */
  static final public HaviType param_dcl() throws ParseException {
        HolderType ht = new HolderType();
        HaviType temp;
    temp = param_attribute();
                            ht.setAttribute(temp);
    temp = param_type_spec();
                            ht.setDataType(temp);
    temp = simple_declarator();
                             ht.setTypeName(temp.getTypeName());
        {if (true) return ht;}
    throw new Error("Missing return statement in function");
  }

/* Production 72 */
  static final public HaviType param_attribute() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 61:
      jj_consume_token(61);
                  {if (true) return new InType();}
      break;
    case 62:
      jj_consume_token(62);
            {
            		if (true)
            		{
            			throw new ParseException("Out paramater is not allow");
            			//return new OutType();
            		}
            }
      break;
    case 63:
      jj_consume_token(63);
                  {if (true)
	                 throw new ParseException("InOut paramater is not allow");
                  	//return new InOutType();
                  }
      break;
    default:
      jj_la1[61] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Production 73 */
  static final public RaiseListType raises_expr() throws ParseException {
        RaiseListType list = new RaiseListType();
        HaviType sname;
    jj_consume_token(64);
    jj_consume_token(28);
    sname = scoped_name();
                                     list.addChild(sname);
    label_22:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 13:
        ;
        break;
      default:
        jj_la1[62] = jj_gen;
        break label_22;
      }
      jj_consume_token(13);
      sname = scoped_name();
      list.addChild(sname);
    }
    jj_consume_token(29);
    {if (true) return list;}
    throw new Error("Missing return statement in function");
  }

/* Production 74 */
  static final public ContextListType context_expr() throws ParseException {
        ContextListType list = new ContextListType();
        HaviType stringName;
    jj_consume_token(65);
    jj_consume_token(28);
    stringName = string_literal();
                                             list.addChild(stringName);
    label_23:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 13:
        ;
        break;
      default:
        jj_la1[63] = jj_gen;
        break label_23;
      }
      jj_consume_token(13);
      stringName = string_literal();
                                                                                                                 list.addChild(stringName);
    }
    jj_consume_token(29);
     {if (true) return list;}
    throw new Error("Missing return statement in function");
  }

/* Production 75 */
  static final public HaviType param_type_spec() throws ParseException {
        HaviType dataType;
        HaviType tempType;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 33:
    case 34:
    case 35:
    case 36:
    case 37:
    case 38:
    case 39:
    case 40:
    case 41:
    case 42:
      dataType = base_type_spec();
                                  {if (true) return dataType;}
      break;
    case 49:
    case 52:
    case 53:
      dataType = template_type_spec();
                                  {if (true) return dataType;}
      break;
    case 14:
    case ID:
      dataType = scoped_name();
         tempType = dataType;
         String typeName = dataType.getTypeName();
         dataType = (HaviType) dataTypeList.get(dataType.getTypeName());

         if(dataType == null)
                dataType = tempType;


         {if (true) return dataType;}
      break;
    default:
      jj_la1[64] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* Definitions of complex regular expressions follow */
  static final public HaviType identifier() throws ParseException {
        Token token;
    token = jj_consume_token(ID);
                  {if (true) return new LiteralType(token.toString()) ;}
    throw new Error("Missing return statement in function");
  }

  static final public HaviType integer_literal() throws ParseException {
        Token token;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OCTALINT:
      token = jj_consume_token(OCTALINT);
                          {if (true) return new LiteralType(token.toString());}
      break;
    case DECIMALINT:
      token = jj_consume_token(DECIMALINT);
                          {if (true) return new LiteralType(token.toString());}
      break;
    case HEXADECIMALINT:
      token = jj_consume_token(HEXADECIMALINT);
                           {if (true) return new LiteralType(token.toString());}
      break;
    default:
      jj_la1[65] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  static final public HaviType string_literal() throws ParseException {
        Token token;
    token = jj_consume_token(STRING);
                          {if (true) return new LiteralType(token.toString());}
    throw new Error("Missing return statement in function");
  }

  static final public HaviType character_literal() throws ParseException {
        Token token;
    token = jj_consume_token(CHARACTER);
                          {if (true) return new LiteralType(token.toString());}
    throw new Error("Missing return statement in function");
  }

  static final public HaviType floating_pt_literal() throws ParseException {
        Token token;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case FLOATONE:
      token = jj_consume_token(FLOATONE);
                          {if (true) return new LiteralType(token.toString());}
      break;
    case FLOATTWO:
      token = jj_consume_token(FLOATTWO);
                          {if (true) return new LiteralType(token.toString());}
      break;
    default:
      jj_la1[66] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  static final private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  static final private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  static final private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  static final private boolean jj_3R_32() {
    if (jj_scan_token(12)) return true;
    return false;
  }

  static final private boolean jj_3R_31() {
    if (jj_3R_33()) return true;
    return false;
  }

  static final private boolean jj_3_3() {
    if (jj_3R_26()) return true;
    return false;
  }

  static final private boolean jj_3R_27() {
    if (jj_scan_token(11)) return true;
    if (jj_3R_29()) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_30()) jj_scanpos = xsp;
    return false;
  }

  static final private boolean jj_3_2() {
    if (jj_3R_25()) return true;
    return false;
  }

  static final private boolean jj_3R_25() {
    if (jj_3R_28()) return true;
    return false;
  }

  static final private boolean jj_3R_28() {
    if (jj_3R_29()) return true;
    Token xsp;
    if (jj_3R_31()) return true;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_31()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  static final private boolean jj_3_1() {
    if (jj_3R_24()) return true;
    return false;
  }

  static final private boolean jj_3R_26() {
    if (jj_scan_token(37)) return true;
    if (jj_scan_token(35)) return true;
    return false;
  }

  static final private boolean jj_3R_33() {
    if (jj_scan_token(54)) return true;
    return false;
  }

  static final private boolean jj_3R_30() {
    if (jj_3R_32()) return true;
    return false;
  }

  static final private boolean jj_3R_24() {
    if (jj_3R_27()) return true;
    if (jj_scan_token(9)) return true;
    return false;
  }

  static final private boolean jj_3R_29() {
    if (jj_scan_token(ID)) return true;
    return false;
  }

  static private boolean jj_initialized_once = false;
  static public IDLParserTokenManager token_source;
  static SimpleCharStream jj_input_stream;
  static public Token token, jj_nt;
  static private int jj_ntk;
  static private Token jj_scanpos, jj_lastpos;
  static private int jj_la;
  static public boolean lookingAhead = false;
  static private boolean jj_semLA;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[67];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static private int[] jj_la1_2;
  static {
      jj_la1_0();
      jj_la1_1();
      jj_la1_2();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x8900,0x8900,0x8900,0x800,0x1000,0xc000,0xc000,0x2000,0x4000,0x4000,0x4000,0x20000,0x40000,0x80000,0x300000,0x300000,0xc00000,0xc00000,0x7000000,0x7000000,0x8c00000,0x8c00000,0xd0004000,0xc0000000,0xc0000000,0x0,0x4000,0x4000,0x0,0x0,0x0,0x2000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x4000,0x4000,0x0,0x0,0x4000,0x0,0x2000,0x2000,0x0,0x0,0x0,0x0,0x0,0x2000,0x4000,0x0,0x0,0x0,0x4000,0x2000,0x0,0x0,0x2000,0x2000,0x4000,0x0,0x0,};
   }
   private static void jj_la1_1() {
      jj_la1_1 = new int[] {0x4011801,0x4011801,0x4011801,0x0,0x0,0x1f331fff,0x1f331fff,0x0,0x0,0x0,0x3003fe,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x11801,0x331ffe,0x3207fe,0x7fe,0x320000,0x11800,0x0,0x0,0x6,0x38,0x18,0x8,0x20,0x8,0xc0,0x331ffe,0x101f8,0xc000,0xc000,0x331ffe,0xc000,0x0,0x0,0x40000,0x40000,0x300000,0x400000,0x1000000,0x0,0x331ffe,0x8000000,0x0,0x0,0x103207fe,0x0,0xe0000000,0xe0000000,0x0,0x0,0x3207fe,0x0,0x0,};
   }
   private static void jj_la1_2() {
      jj_la1_2 = new int[] {0x0,0x0,0x0,0x0,0x0,0x4,0x4,0x0,0x0,0x0,0x4,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x3fc,0x3f8,0x0,0x0,0x4,0x4,0x0,0x0,0x0,0x0,0x4,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x4,0x4,0x0,0x0,0x4,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x4,0x0,0x1,0x2,0x4,0x0,0x0,0x0,0x0,0x0,0x4,0x38,0xc0,};
   }
  static final private JJCalls[] jj_2_rtns = new JJCalls[3];
  static private boolean jj_rescan = false;
  static private int jj_gc = 0;

  public IDLParser(java.io.InputStream stream) {


    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static com.streetfiresound.codegenerator.parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during com.streetfiresound.codegenerator.parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new IDLParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 67; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  static public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 67; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public IDLParser(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static com.streetfiresound.codegenerator.parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during com.streetfiresound.codegenerator.parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new IDLParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 67; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 67; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public IDLParser(IDLParserTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static com.streetfiresound.codegenerator.parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during com.streetfiresound.codegenerator.parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 67; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(IDLParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 67; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  static final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  static final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  static final private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }

  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  static final public Token getToken(int index) {
    Token t = lookingAhead ? jj_scanpos : token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.Vector jj_expentries = new java.util.Vector();
  static private int[] jj_expentry;
  static private int jj_kind = -1;
  static private int[] jj_lasttokens = new int[100];
  static private int jj_endpos;

  static private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      boolean exists = false;
      for (java.util.Enumeration e = jj_expentries.elements(); e.hasMoreElements();) {
        int[] oldentry = (int[])(e.nextElement());
        if (oldentry.length == jj_expentry.length) {
          exists = true;
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              exists = false;
              break;
            }
          }
          if (exists) break;
        }
      }
      if (!exists) jj_expentries.addElement(jj_expentry);
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  static public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[74];
    for (int i = 0; i < 74; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 67; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
          if ((jj_la1_2[i] & (1<<j)) != 0) {
            la1tokens[64+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 74; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  static final public void enable_tracing() {
  }

  static final public void disable_tracing() {
  }

  static final private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 3; i++) {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
          }
        }
        p = p.next;
      } while (p != null);
    }
    jj_rescan = false;
  }

  static final private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
