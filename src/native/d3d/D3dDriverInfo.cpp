/*
 * $RCSfile$
 *
 * Copyright 2000-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision$
 * $Date$
 * $State$
 */

/* j3dsys.h needs to be included before any other include files to suppres VC warning */
#include "j3dsys.h"
#include "Stdafx.h"


const DWORD numDeviceTypes = 2;
const D3DDEVTYPE deviceTypes[2] = { D3DDEVTYPE_HAL, D3DDEVTYPE_REF };
D3dDriverInfo **d3dDriverList = NULL;
int numDriver = 0; // size of above array list

int requiredDeviceID = -1;   // must use this Device or die   

// If this number is greater than zero, J3D must use the 
// adapter index in the order of GetAdapterIdentifier() starting from one.
// Otherwise, the first driver found with monitor matching the display
// driver is used.
int requiredDriverID = -1;  
OSVERSIONINFO osvi;

// There is bug in Nvidia driver which prevent VB too big
// in TnL hardware vertex processing mode. 
// When the index is greater than 65535, the nvidia driver will
// consider it to be N % 65535. However it works fine in
// hardware vertex processing mode.
//@TODO check this with Cap Bits
UINT vertexBufferMaxVertexLimit = 65535;

// True to disable setting D3DRS_MULTISAMPLEANTIALIAS 
// Rendering state.
BOOL implicitMultisample; 

// Fix to Issue 226 : D3D - fail on stress test for the creation and destruction of Canvases
D3DFORMAT d3dDepthFormat[D3DDEPTHFORMATSIZE] = { D3DFMT_D24S8, // this is the best choice
                                                 D3DFMT_D24X4S4,
                                                 D3DFMT_D15S1,
                                                 D3DFMT_D24X8,
                                                 D3DFMT_D16_LOCKABLE,
                                                 D3DFMT_D16,
                                                 D3DFMT_D32
};

// This should match the depth bit in the above array
int d3dDepthTable[D3DDEPTHFORMATSIZE]        = {24, 24, 15, 24, 16, 16, 32};
int d3dStencilDepthTable[D3DDEPTHFORMATSIZE] = { 8,  4,  1,  0,  0,  0,  0};

D3DLIGHT9 ambientLight; 

D3dDriverInfo::D3dDriverInfo()
{
    for (int i=0; i < numDeviceTypes; i++) {
	d3dDeviceList[i] = new D3dDeviceInfo();
    }
}

D3dDriverInfo::~D3dDriverInfo()
{
    for (int i=0; i < numDeviceTypes; i++) {
	SafeDelete(d3dDeviceList[i]);
    }
}

VOID computeRGBDepth(D3dDriverInfo *pDriver)
{
    switch (pDriver->desktopMode.Format) {
        case D3DFMT_R8G8B8:
        case D3DFMT_A8R8G8B8: 
        case D3DFMT_X8R8G8B8:
	    pDriver->redDepth = pDriver->greenDepth =
		pDriver->blueDepth = 8;
	    break;
        case D3DFMT_R5G6B5:
	    pDriver->redDepth = pDriver->blueDepth = 5;
	    pDriver->greenDepth = 6;
	    break;
        case D3DFMT_X1R5G5B5:
        case D3DFMT_A1R5G5B5: 
	    pDriver->redDepth = pDriver->blueDepth =
		pDriver->greenDepth = 5;	
	    break;
        case D3DFMT_A4R4G4B4:
        case D3DFMT_X4R4G4B4:
	    pDriver->redDepth = pDriver->blueDepth =
		pDriver->greenDepth = 4;		
	    break;
        case D3DFMT_R3G3B2:
        case D3DFMT_A8R3G3B2:
	    pDriver->redDepth = pDriver->greenDepth = 3;		
	    pDriver->blueDepth = 2;
		break;
        default: // 8 color indexed or less
	    pDriver->redDepth = pDriver->blueDepth =
		pDriver->greenDepth = 0;			
    }

}


VOID setInfo(D3dDeviceInfo* pDevice,D3DADAPTER_IDENTIFIER9 *identifier)
{ 
	 char* str = (char *)"UNKNOW VGA Vendor.";	
     switch( identifier->VendorId )
     {
     // A more complete list can be found from http://www.pcidatabase.com/vendors.php?sort=id
     case 0x0000:   str = (char *) "RDP Remote Desktop DirectDraw";        break;
     case 0x1002:   str = (char *) "ATI Technologies Inc.";                break;
	 case 0x1004:   str = (char *) "VLSI Technology.";                     break;
     case 0x100B:   str = (char *) "National Semiconductors.";             break;
     case 0x1013:   str = (char *) "Cirrus Logic.";                        break;
	 case 0x1014:   str = (char *) "IBM";								   break;
     case 0x1017:   str = (char *) "Spea Software AG.";                    break;

     case 0x1022:   str = (char *) "Advanced Micro Devices (AMD).";        break;
	 case 0x1025:   str = (char *) "ALI Acer Labs Inc.";                   break;
     case 0x1023:   str = (char *) "Trident Microsistems.";                break;
	 case 0x102C:   str = (char *) "Asiliant (Chips And Technologies).";   break;
     case 0x102B:   str = (char *) "Matrox Electronic Systems Ltd.";       break;
	 case 0x1031:   str = (char *) "MIRO Computer Products AG";            break;
	 case 0x1033:   str = (char *) "NEC Electronics.";                     break;
	 case 0x1039:   str = (char *) "SiS Silicon Integrated Systems.";      break;
	 case 0x1045:   str = (char *) "OPTi";                                 break;
     case 0x104C:   str = (char *) "Texas Instruments.";                   break;
     case 0x104E:   str = (char *) "Oak Technology.";                      break;
	 case 0x1050:   str = (char *) "Winbond Electronics Corp.";            break;
	 case 0x105D:   str = (char *) "Number Nine Visual Technology";        break;
     case 0x1060:   str = (char *) "United Microelectronics.";             break;
	 case 0x106B:   str = (char *) "Apple Computer,Inc.";                  break;
     case 0x1073:   str = (char *) "Yamaha Corporation.";                  break;

     case 0x108E:   str = (char *) "Sun Microsystems.";                    break;
     case 0x1091:   str = (char *) "Intergraph Corporation.";              break;

     case 0x10DE:   str = (char *) "NVIDIA Corporation.";                  break;
	 case 0x10EA:   str = (char *) "Tvia, Inc.";                           break;
	 case 0x10ED:   str = (char *) "Ascii Corporation";                    break;
	 case 0x1092:   str = (char *) "Diamond Multimedia";                    break;
     case 0x1102:   str = (char *) "Creative Technology LTD.";             break;
	 case 0x1117:   str = (char *) "Datacube Inc.";                        break;
	 case 0x1106:   str = (char *) "VIA Technology. ";                     break;
     case 0x1135:   str = (char *) "Fuji Xerox Co Ltd.";                   break;
	 case 0x1142:   str = (char *) "Alliance Semiconductor";               break;
	 case 0x117f:   str = (char *) "Integrated Circuit Systems.";          break;
     case 0x11AE:   str = (char *) "Scitex Corporation Ltd.";              break;

     case 0x121A:   str = (char *) "3dfx Interactive Inc.";                break;
     case 0x1251:   str = (char *) "VLSI Solution OY.";                    break;
	 case 0x126F:   str = (char *) "Silicon Motion.";                      break;
	 case 0x1295:   str = (char *) "Imagination Technologies.";            break;
	 case 0x1414:   str = (char *) "MicroSoft.";                           break;
	 case 0x15AD:   str = (char *) "VMware Inc.";                          break;
	 case 0x18CA:   str = (char *) "XGI xgitech.";                         break;	 
	 case 0x1888:   str = (char *) "Varisys Limited";                      break;

	 case 0x1B13:   str = (char *) "Jaton Corporation USA.";               break;
	 case 0x3D3D:   str = (char *) "3Dlabs Inc, Ltd.";                     break;
     case 0x5333:   str = (char *) "S3 Graphics Co., Ltd.";                break;
     case 0x8086:   str = (char *) "Intel Corporation.";                   break;
     case 0xAA42:   str = (char *) "Scitex Digital Video";                 break;

	 case 0xDEAF:   str = (char *) "Middle Digital, Inc.";                 break;
	 case 0xEDD8:   str = (char *) "ARK Logic, Inc.";                      break;
	 case 0xFFF3:   str = (char *) "VMWare Inc.(Legacy)";                  break;   
     }	
     pDevice->deviceVendor = str;
        
     pDevice->deviceRenderer = identifier->Description;  
     // better handling of RDP - Remote Desktop
	 if(identifier->VendorId==0 && identifier->DeviceId==0){
		 pDevice->deviceRenderer = (char *) "RDP - No Hardware D3D"; 
	 }

     char version[ 128 ];
     sprintf( version, "%x.%x.%x.%x", HIWORD( identifier->DriverVersion.HighPart ),
		      LOWORD( identifier->DriverVersion.HighPart ), 
			  HIWORD( identifier->DriverVersion.LowPart ), 
			  LOWORD( identifier->DriverVersion.LowPart ) );
     pDevice->deviceVersion = (char *)version;	
	
}


VOID buildDriverList(LPDIRECT3D9 pD3D)
{
    numDriver =  pD3D->GetAdapterCount();
    if (numDriver <= 0) {
        // keep d3dDriverList = NULL for checking later
        D3dCtx::d3dError(DRIVERNOTFOUND);
        return;
    }
    
    d3dDriverList = new LPD3dDriverInfo[numDriver];
    
    if (d3dDriverList == NULL) {
        D3dCtx::d3dError(OUTOFMEMORY);
        return;
    }
    
    D3dDriverInfo *pDriver;
    
    for (int i = 0; i < numDriver; i++ ) {
        pDriver = new D3dDriverInfo();
        d3dDriverList[i] = pDriver;
        pD3D->GetAdapterIdentifier(i, 0, &pDriver->adapterIdentifier);
        pD3D->GetAdapterDisplayMode(i, &pDriver->desktopMode);
        computeRGBDepth(pDriver);
        pDriver->hMonitor = pD3D->GetAdapterMonitor(i);
        pDriver->iAdapter = i;
        
        
        for (int j = 0; j < numDeviceTypes; j++ ) {
            D3DCAPS9 d3dCaps;
            D3dDeviceInfo* pDevice = pDriver->d3dDeviceList[j];
            pDevice->deviceType = deviceTypes[j];
            pD3D->GetDeviceCaps(i, deviceTypes[j], &d3dCaps);
            pDevice->setCaps(&d3dCaps);

            pDevice->desktopCompatible = 
            SUCCEEDED(pD3D->CheckDeviceType(i, deviceTypes[j],
            pDriver->desktopMode.Format,
            pDriver->desktopMode.Format,
            TRUE));

            pDevice->fullscreenCompatible =
            SUCCEEDED(pD3D->CheckDeviceType(i, deviceTypes[j],
            pDriver->desktopMode.Format,
            pDriver->desktopMode.Format,
            FALSE));
            
            pDevice->maxZBufferDepthSize = 0;
            

            if (pDevice->isHardwareTnL) {
                strcpy(pDevice->deviceName, "Transform & Light Hardware Rasterizer");
            } else if (pDevice->isHardware) {
                strcpy(pDevice->deviceName, "Hardware Rasterizer");
            } else {
                strcpy(pDevice->deviceName, "Reference Rasterizer");
            }
            //issue 135 put here info about vendor and device model
            setInfo(pDevice, &pDriver->adapterIdentifier);

            for (int k=0; k < D3DDEPTHFORMATSIZE; k++) {

                pDevice->depthFormatSupport[k] =
                SUCCEEDED(pD3D->CheckDeviceFormat(i, deviceTypes[j],
                pDriver->desktopMode.Format,
                D3DUSAGE_DEPTHSTENCIL,
                D3DRTYPE_SURFACE,
                d3dDepthFormat[k]))
                &&
                SUCCEEDED(pD3D->CheckDepthStencilMatch(i, deviceTypes[j],
                pDriver->desktopMode.Format,
                pDriver->desktopMode.Format,
                d3dDepthFormat[k]));
                if (pDevice->depthFormatSupport[k]) {
                    if (d3dDepthTable[k] > pDevice->maxZBufferDepthSize) {
                        pDevice->maxZBufferDepthSize = d3dDepthTable[k];
                        pDevice->maxStencilDepthSize = d3dStencilDepthTable[k];
                        if (d3dStencilDepthTable[k]>0) {
                            pDevice->supportStencil = true;
                        }
                        else {
                            pDevice->supportStencil = false;
                        }
                    }
                }
            }
	    // Fix to Issue 226 : D3D - fail on stress test for the creation and destruction of Canvases
            DWORD bitmask = 0;
            pDevice->multiSampleSupport = 0;
            for (int mtype = D3DMULTISAMPLE_2_SAMPLES;
            mtype <= D3DMULTISAMPLE_16_SAMPLES; mtype++) {
                // consider desktop mode only for multisampling
                HRESULT hrMSBack;
                // HRESULT hrMSDepth; // should also check DephStencil ??? TODO - aces
                
                bitmask = 1 << mtype;
                hrMSBack = pD3D->CheckDeviceMultiSampleType(i, deviceTypes[j],
                pDriver->desktopMode.Format,
                TRUE,
                (D3DMULTISAMPLE_TYPE) mtype, NULL);
                if(SUCCEEDED(hrMSBack)) {
                    pDevice->multiSampleSupport |= bitmask;
                    if(debug){
                        /*
                         printf("Multisample: Back  %s uses  %s at bitmask %d, mtype %d, multiSampleSupport %d  \n",
                         getPixelFormatName(pDriver->desktopMode.Format),
                         getMultiSampleName((D3DMULTISAMPLE_TYPE) mtype),
                         bitmask, mtype, pDevice->multiSampleSupport  );
                         */
                    }
                }//if hrMSBack
                
            }// for mtype
        }//for j - device Types
    }// for i - numDriver

}



// Cleanup when no more ctx exists 
VOID D3dDriverInfo::release()
{
    for (int i = 0; i < numDriver; i++ ) {
	SafeDelete(d3dDriverList[i]);
    }    
    SafeDelete(d3dDriverList);
    numDriver = 0;
}

VOID printInfo() 
{
    printf("javax.media.j3d 1.5.2, Windows version is %d.%d ", 
	   osvi.dwMajorVersion, osvi.dwMinorVersion);
    printf("Build: %d, ", LOWORD(osvi.dwBuildNumber)); 
    
    switch(osvi.dwPlatformId) {
    case VER_PLATFORM_WIN32s:
	printf("Windows3.1");
	break;
    case VER_PLATFORM_WIN32_WINDOWS:
	printf("Windows 95/98");
	break;
    case VER_PLATFORM_WIN32_NT:		
	//printf("Windows NT/2000/XP");
		if(osvi.dwMajorVersion==5){
            printf("Windows NT/2000/XP");
			}
		if(osvi.dwMajorVersion==6){
            printf("Windows Server 2008/Vista");
			}
	break;
    }

    printf(" %s", osvi.szCSDVersion);

    D3dDriverInfo *pDriver;
    for (int i=0; i < numDriver; i++) {
	pDriver  = d3dDriverList[i];
	D3DADAPTER_IDENTIFIER9 *id = &pDriver->adapterIdentifier;
	D3DDISPLAYMODE *dm = &pDriver->desktopMode;
        printf("\n[Display Driver] %s, %s, Product %d\n",
	       id->Driver, id->Description,
	       HIWORD(id->DriverVersion.HighPart));
	printf("                 Version %d.%d, Build %d, VendorId %x\n",
	       LOWORD(id->DriverVersion.HighPart),	       
	       HIWORD(id->DriverVersion.LowPart),	       
	       LOWORD(id->DriverVersion.LowPart),		   
		   id->VendorId);
	printf("                 DeviceId %x, SubSysId %x, Revision %d\n",
	       id->VendorId, id->DeviceId,
	       id->SubSysId, id->Revision);
	printf("  [Desktop Mode] %dx%d ", 
	       dm->Width, dm->Height);

	if (dm->RefreshRate != 0) {
	    printf("%d MHz", dm->RefreshRate);
	}
	printf(", %s\n", getPixelFormatName(dm->Format));

	for (int j=0; j < numDeviceTypes; j++) {
	    D3dDeviceInfo *pDevice = pDriver->d3dDeviceList[j];
	    printf("\t[Device] %s ", pDevice->deviceName);
	    if (pDevice->multiSampleSupport) {
		printf("(AA)");
	    }
	    printf("\n");
	}
    }
    printf("\n");
}


// Construct the D3dDriverList by enumerate all the drivers
VOID D3dDriverInfo::initialize(JNIEnv *env) 
{
    HINSTANCE hD3D9DLL = LoadLibrary( "D3D9.DLL" );

    // Simply see if D3D9.dll exists.
    if ( hD3D9DLL == NULL )
    {
	D3dCtx::d3dError(D3DNOTFOUND);
	return;
    }
    FreeLibrary(hD3D9DLL);
	
    LPDIRECT3D9 pD3D = Direct3DCreate9( D3D_SDK_VERSION );
	if (debug && pD3D != NULL){
		printf("[j3d] Using DirectX D3D 9.0 or higher.\n");
		printf("[j3d] DirectX D3D renderer build 1.5.2\n");
	}
    if (pD3D == NULL) {
	D3dCtx::d3dError(D3DNOTFOUND);
	return;
    }

    // must appear before buildDriverList in order to
    // set VertexBufferLimit correctly in D3dDeviceInfo
    D3dCtx::setVBLimitProperty(env);
    buildDriverList(pD3D);

    SafeRelease(pD3D);

    //D3dCtx::setDebugProperty(env);
    D3dCtx::setDebugProperty(env);

    osvi.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
    GetVersionEx(&osvi);

    if (debug) {
	printInfo();
    }

    // compute requiredGUID  
    D3dCtx::setDeviceFromProperty(env);

    D3dCtx::setImplicitMultisamplingProperty(env);

    /*
    RasterList.init();
    BackgroundImageList.init();
    */

    // Setup Global constant Ambient light 
    ambientLight.Type = D3DLIGHT_DIRECTIONAL;
    ambientLight.Direction.x = 0;
    ambientLight.Direction.y = 0;
    ambientLight.Direction.z = 1;
    CopyColor(ambientLight.Diffuse, 0, 0, 0, 1.0f);
    CopyColor(ambientLight.Ambient, 1.0f, 1.0f, 1.0f, 1.0f);
    CopyColor(ambientLight.Specular, 0, 0, 0, 1.0f);
}



