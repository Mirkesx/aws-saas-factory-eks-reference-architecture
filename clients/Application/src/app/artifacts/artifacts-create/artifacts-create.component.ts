/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Product } from '../../products/models/product.interface';
import { ProductService } from '../../products/product.service';
import { Artifact } from '../models/artifact.interface';
import { ArtifactsService } from '../artifacts.service';

interface LineItem {
  product: Product;
  quantity?: number;
}

@Component({
  selector: 'app-artifacts-create',
  templateUrl: './artifacts-create.component.html',
  styles: [
    '.dottedUnderline { bartifact-bottom: 1px dotted; }',
  ]
})
export class ArtifactsCreateComponent implements OnInit {
  artifactForm: FormGroup;
  artifactProducts: LineItem[] = [];
  error: string;

  constructor(private fb: FormBuilder,
              private router: Router,
              private productSvc: ProductService,
              private artifactSvc: ArtifactsService) { }

  ngOnInit(): void {
    this.productSvc.fetch().subscribe(products => {
      this.artifactProducts = products.map(p => (
        { product: p }
      ));
    })
    this.artifactForm = this.fb.group({
      name: ['', Validators.required],
    });
  }

  add(op: LineItem) {
    const artifactProduct = this.artifactProducts.find(p => p?.product.productId  === op.product.productId);
    this.artifactProducts = this.artifactProducts.map(p => {
      if (p.product?.productId === artifactProduct.product?.productId) {
        p = {
          ...artifactProduct,
          quantity: artifactProduct.quantity ? artifactProduct.quantity + 1 : 1,
        };
      }
      return p;
    });
  }

  remove(op: LineItem) {
    const artifactProduct = this.artifactProducts.find(p => p?.product.productId  === op.product.productId);
    this.artifactProducts = this.artifactProducts.map(p => {
      if (p.product?.productId === artifactProduct.product?.productId) {
        p = {
          ...artifactProduct,
          quantity: artifactProduct.quantity && artifactProduct.quantity > 1 ?
                      artifactProduct.quantity - 1 : undefined,
        };
      }
      return p;
    });
  }

  submit() {
    const val: Artifact = {
      ...this.artifactForm.value,
      artifactProduct: this.artifactProducts
      .filter(p => !!p.quantity)
      .map(p => ({
        productId: p.product.productId,
        price: p.product.price,
        quantity: p.quantity
      })),
    };
    this.artifactSvc.create(val)
      .subscribe(() => {
        this.router.navigate(['artifacts']);
      },
      (err: string) => {
        this.error = err;
      });
  }

  cancel() {
    this.router.navigate(['artifacts']);
  }

}
