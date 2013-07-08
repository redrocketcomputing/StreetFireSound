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
 * $Id: RuleFactory.java,v 1.2 2005/02/24 03:03:34 stephen Exp $
 */

package com.streetfiresound.codegenerator.rules;


import com.streetfiresound.codegenerator.types.AddType;
import com.streetfiresound.codegenerator.types.AndType;
import com.streetfiresound.codegenerator.types.AnyType;
import com.streetfiresound.codegenerator.types.ArrayType;
import com.streetfiresound.codegenerator.types.BooleanType;
import com.streetfiresound.codegenerator.types.CharType;
import com.streetfiresound.codegenerator.types.ConstType;
import com.streetfiresound.codegenerator.types.ConstTypeDefinition;
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
import com.streetfiresound.codegenerator.types.InOutType;
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
import com.streetfiresound.codegenerator.types.OutType;
import com.streetfiresound.codegenerator.types.ParameterListType;
import com.streetfiresound.codegenerator.types.RaiseListType;
import com.streetfiresound.codegenerator.types.SequenceType;
import com.streetfiresound.codegenerator.types.ShiftType;
import com.streetfiresound.codegenerator.types.ShortType;
import com.streetfiresound.codegenerator.types.StringType;
import com.streetfiresound.codegenerator.types.StructType;
import com.streetfiresound.codegenerator.types.TypedefType;
import com.streetfiresound.codegenerator.types.ULongLongType;
import com.streetfiresound.codegenerator.types.ULongType;
import com.streetfiresound.codegenerator.types.UShortType;
import com.streetfiresound.codegenerator.types.UnaryType;
import com.streetfiresound.codegenerator.types.UnionType;
import com.streetfiresound.codegenerator.types.VoidType;
import com.streetfiresound.codegenerator.types.WCharType;
import com.streetfiresound.codegenerator.types.WStringType;
import com.streetfiresound.codegenerator.types.XorType;

/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class RuleFactory implements MakeRuleObjectInterface, ConstTypeDefinition
{

  /**
   * Constructor for RuleFactory.
   */
  public RuleFactory()
  {
    super();
  }

  /**
   * @see com.streetfiresound.codegenerator.rules.MakeRuleObjectInterface#createRuleObject(int)
   */
  public RuleDefinition createRuleObject(short key, HaviType ht)
  {

    switch(key)
    {
         case INTERFACE:

            return new InterfaceRule((InterfaceType) ht);

         case BOOLEAN:

            return new BooleanRule((BooleanType) ht);

         case SHORT:

            return new ShortRule((ShortType) ht);

         case LONG:

            return new LongRule((LongType) ht);

         case LONGLONG:

            return new LongLongRule((LongLongType) ht);

         case ULONG:

            return new ULongRule((ULongType) ht);

         case ULONGLONG:

            return new ULongLongRule((ULongLongType) ht);

         case USHORT:

            return new UShortRule((UShortType) ht);

         case STRUCT:

            return new StructRule((StructType) ht);

         case MODULE:

            return new ModuleRule((ModuleType) ht);

         case CONTEXT:

            return new ContextRule((ContextType) ht);

         case DECLARATION:

            return new DeclarationRule((DeclarationType) ht);

         case FUNCTION:

            return new FunctionRule((FunctionType) ht);

         case STRING:

            return new StringRule((StringType) ht);

         case WSTRING:

            return new WStringRule((WStringType) ht);

         case OCTET:

            return new OctetRule((OctetType) ht);

         case CHAR:

            return new CharRule((CharType) ht);

         case WCHAR:

            return new WCharRule((WCharType) ht);

         case VOID:

            return new VoidRule((VoidType) ht);

         case ENUM:

            return new EnumRule((EnumType) ht);

         case UNION:

            return new UnionRule((UnionType) ht);

         case SEQUENCE:

            return new SequenceRule((SequenceType) ht);

         case ARRAY:

            return new ArrayRule((ArrayType) ht);


         case TYPEDEF:

            return new TypedefRule((TypedefType) ht);

         case CONST:

            return new ConstRule((ConstType) ht);

         case IN:

            return new InRule((InType) ht);

         case OUT:

            return new OutRule((OutType) ht);

         case INOUT:

            return new InOutRule((InOutType) ht);

         case LITERAL:

            return new LiteralRule((LiteralType) ht);

         case PARAMETERLIST:

            return new ParameterListRule((ParameterListType) ht);

         case HOLDER:

            return new HolderRule((HolderType) ht);

         case FLOAT:

            return new FloatRule((FloatType) ht);

         case DOUBLE:

            return new DoubleRule((DoubleType) ht);

         case ANY:

            return new AnyRule((AnyType) ht);

         case FORWARDDEC:

            return new ForwardDeclarationRule((ForwardDeclarationType) ht);

         case INTERFACEHEADER:

            return new InterfaceHeaderRule((InterfaceHeaderType) ht);

         case OR:

            return new OrRule((OrType) ht);

         case MULT:

            return new MultRule((MultType) ht);

         case XOR:

            return new XorRule((XorType) ht);

         case ADD:

            return new AddRule((AddType) ht);

         case SHIFT:

            return new ShiftRule((ShiftType) ht);

         case AND:

            return new AndRule((AndType) ht);

         case UNARY:

            return new UnaryRule((UnaryType) ht);

         case EXCEPTION:

            return new ExceptionRule((ExceptionType) ht);

         case RAISELIST:

            return new RaiseListRule((RaiseListType) ht);

         case CONTEXTLIST:

            return new ContextListRule((ContextListType) ht);

         default:

            return null;
    }


  }

}
